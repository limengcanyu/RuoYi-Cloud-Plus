package org.dromara.common.http.config;

import cn.dev33.satoken.same.SaSameUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.ServletUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.annotation.RemoteHttpService;
import org.dromara.common.http.annotation.RemoteServiceController;
import org.dromara.common.http.log.aspect.RemoteHttpProviderLogAspect;
import org.dromara.common.http.handler.RemoteHttpExceptionHandler;
import org.dromara.common.http.properties.RemoteHttpProperties;
import org.dromara.common.http.registrar.RemoteHttpServiceRegistrar;
import org.dromara.common.http.support.RemoteHttpFallbackProxyPostProcessor;
import org.dromara.common.http.log.support.LoggingHttpExchangeAdapter;
import org.dromara.common.http.log.support.RemoteHttpLogSupport;
import org.dromara.common.json.utils.JsonUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.aop.framework.autoproxy.AutoProxyUtils;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 内部 HTTP 远程调用配置.
 *
 * 这里把运行时几条链路接起来:
 * 1. Consumer 发请求前透传认证头和 Seata XID
 * 2. 远程非 2xx 响应统一转成 ServiceException
 * 3. 打开请求日志时，为 consumer/provider 两侧挂日志能力
 * 4. 远程代理失败时按接口声明触发 fallback
 *
 * @author Lion Li
 */
@Slf4j
@AutoConfiguration
@Import(RemoteHttpServiceRegistrar.class)
@EnableConfigurationProperties(RemoteHttpProperties.class)
public class RemoteHttpAutoConfiguration {

    @Bean
    public static BeanFactoryPostProcessor remoteHttpControllerProxyCompatibilityPostProcessor() {
        return new RemoteHttpInfrastructurePostProcessor();
    }

    @Bean("remoteHttpHeaderInterceptor")
    public ClientHttpRequestInterceptor remoteHttpHeaderInterceptor() {
        return (request, body, execution) -> {
            HttpHeaders headers = request.getHeaders();
            HttpServletRequest currentRequest = ServletUtils.getRequest();
            if (currentRequest != null) {
                String authorization = currentRequest.getHeader(HttpHeaders.AUTHORIZATION);
                if (StringUtils.isNotBlank(authorization)) {
                    headers.set(HttpHeaders.AUTHORIZATION, authorization);
                }
            }
            try {
                // 透传 same-token，保证服务间调用仍然走内网鉴权。
                headers.set(SaSameUtil.SAME_TOKEN, SaSameUtil.getToken());
            } catch (Exception ignored) {
            }
            relaySeataXid(headers);
            return execution.execute(request, body);
        };
    }

    @Bean
    public RestClientHttpServiceGroupConfigurer remoteHttpServiceGroupConfigurer(
        ClientHttpRequestInterceptor remoteHttpHeaderInterceptor,
        RemoteHttpLogSupport remoteHttpLogSupport) {
        return groups -> groups.forEachGroup((group, clientBuilder, proxyFactoryBuilder) -> {
            clientBuilder.requestInterceptor(remoteHttpHeaderInterceptor)
                // provider 侧远程接口异常会直接映射成非 2xx，这里只按 HTTP 状态处理即可。
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    throwServiceException(response.getStatusCode().value(), response.getStatusText(), readResponseBody(response));
                });
            if (remoteHttpLogSupport.isEnabled()) {
                // consumer 侧日志挂在 HttpExchangeAdapter 上，避免碰底层 body 重复读取问题。
                proxyFactoryBuilder.exchangeAdapterDecorator(adapter -> new LoggingHttpExchangeAdapter(adapter, remoteHttpLogSupport));
            }
        });
    }

    @Bean
    public RemoteHttpFallbackProxyPostProcessor remoteHttpFallbackProxyPostProcessor() {
        return new RemoteHttpFallbackProxyPostProcessor();
    }

    @Bean
    public RemoteHttpLogSupport remoteHttpLogSupport(RemoteHttpProperties properties) {
        return new RemoteHttpLogSupport(properties);
    }

    @Bean
    public RemoteHttpProviderLogAspect remoteHttpProviderLogAspect(RemoteHttpLogSupport remoteHttpLogSupport) {
        return new RemoteHttpProviderLogAspect(remoteHttpLogSupport);
    }

    @Bean
    public RemoteHttpExceptionHandler remoteHttpExceptionHandler() {
        return new RemoteHttpExceptionHandler();
    }

    private void relaySeataXid(HttpHeaders headers) {
        try {
            // 通过反射做可选适配，未引入 Seata 时不强依赖该类。
            Class<?> rootContextClass = Class.forName("org.apache.seata.core.context.RootContext");
            String xid = (String) rootContextClass.getMethod("getXID").invoke(null);
            if (StringUtils.isBlank(xid)) {
                return;
            }
            String headerName = (String) rootContextClass.getField("KEY_XID").get(null);
            headers.set(headerName, xid);
        } catch (ClassNotFoundException ignored) {
        } catch (Exception e) {
            log.debug("relay seata xid failed", e);
        }
    }

    private String readResponseBody(org.springframework.http.client.ClientHttpResponse response) {
        try {
            return StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.debug("read remote response body failed", e);
            return null;
        }
    }

    private void throwServiceException(int statusCode, String statusText, String responseBody) {
        if (StringUtils.isNotBlank(responseBody) && JsonUtils.isJsonObject(responseBody)) {
            try {
                // 远程服务如果按 R 返回错误信息，优先还原成更友好的业务异常消息。
                R<?> result = JsonUtils.parseObject(responseBody, R.class);
                if (result != null && (result.getCode() == 0 || R.isSuccess(result))) {
                    return;
                }
                if (result != null && StringUtils.isNotBlank(result.getMsg())) {
                    throw new ServiceException(result.getMsg(), result.getCode());
                }
            } catch (ServiceException se) {
                throw se;
            } catch (RuntimeException e) {
                log.debug("parse remote error body failed: {}", responseBody, e);
            }
        }
        String message = StringUtils.firstNonBlank(responseBody, statusText, "远程服务调用失败");
        throw new ServiceException(message, statusCode);
    }

    private static final class RemoteHttpInfrastructurePostProcessor implements BeanFactoryPostProcessor {

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
            BeanDefinitionRegistry registry = beanFactory instanceof BeanDefinitionRegistry beanDefinitionRegistry
                ? beanDefinitionRegistry : null;
            ClassLoader beanClassLoader = beanFactory.getBeanClassLoader();
            for (String beanName : beanFactory.getBeanDefinitionNames()) {
                BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
                preserveRemoteControllerTargetClass(beanDefinition);
                registerFallbackBeanDefinition(registry, beanFactory, beanDefinition, beanClassLoader);
            }
        }

        private void preserveRemoteControllerTargetClass(BeanDefinition beanDefinition) {
            if (!(beanDefinition instanceof AnnotatedBeanDefinition annotatedBeanDefinition)) {
                return;
            }
            if (!annotatedBeanDefinition.getMetadata().hasAnnotation(RemoteServiceController.class.getName())) {
                return;
            }
            beanDefinition.setAttribute(AutoProxyUtils.PRESERVE_TARGET_CLASS_ATTRIBUTE, Boolean.TRUE);
        }

        private void registerFallbackBeanDefinition(BeanDefinitionRegistry registry,
            ConfigurableListableBeanFactory beanFactory, BeanDefinition beanDefinition, ClassLoader beanClassLoader) {
            if (registry == null) {
                return;
            }
            Class<?> serviceInterface = resolveRemoteServiceInterface(beanDefinition, beanClassLoader);
            if (serviceInterface == null) {
                return;
            }
            RemoteHttpService remoteHttpService = serviceInterface.getAnnotation(RemoteHttpService.class);
            if (remoteHttpService == null || remoteHttpService.fallback() == void.class) {
                return;
            }
            Class<?> fallbackClass = remoteHttpService.fallback();
            if (!serviceInterface.isAssignableFrom(fallbackClass)) {
                throw new IllegalStateException("Fallback class must implement remote service interface: "
                    + fallbackClass.getName() + " -> " + serviceInterface.getName());
            }
            if (beanFactory.getBeanNamesForType(fallbackClass, false, false).length > 0) {
                return;
            }
            BeanDefinition fallbackBeanDefinition = BeanDefinitionBuilder.genericBeanDefinition(fallbackClass)
                .setLazyInit(true)
                .getBeanDefinition();
            // fallback 只给框架内部按具体类型获取使用，不参与业务侧按接口类型自动注入，
            // 否则会和真正的远程代理一起成为 RemoteXxxService 的候选 Bean。
            fallbackBeanDefinition.setAutowireCandidate(false);
            fallbackBeanDefinition.setPrimary(false);
            registry.registerBeanDefinition(fallbackClass.getName(), fallbackBeanDefinition);
        }

        private Class<?> resolveRemoteServiceInterface(BeanDefinition beanDefinition, ClassLoader beanClassLoader) {
            String beanClassName = beanDefinition.getBeanClassName();
            if (beanClassName == null || beanClassLoader == null) {
                return null;
            }
            Class<?> beanClass = org.springframework.util.ClassUtils.resolveClassName(beanClassName, beanClassLoader);
            if (!beanClass.isInterface() || !beanClass.isAnnotationPresent(RemoteHttpService.class)) {
                return null;
            }
            return beanClass;
        }
    }
}
