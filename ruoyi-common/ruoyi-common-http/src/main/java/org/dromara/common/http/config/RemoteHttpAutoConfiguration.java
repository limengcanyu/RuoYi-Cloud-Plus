package org.dromara.common.http.config;

import cn.dev33.satoken.same.SaSameUtil;
import feign.Logger;
import feign.RequestInterceptor;
import feign.Response;
import feign.codec.ErrorDecoder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.http.annotation.RemoteServiceController;
import org.dromara.common.http.log.aspect.RemoteHttpProviderLogAspect;
import org.dromara.common.http.handler.RemoteHttpExceptionHandler;
import org.dromara.common.http.log.support.RemoteHttpFeignLogger;
import org.dromara.common.http.log.support.RemoteHttpLogSupport;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.core.utils.ServletUtils;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.aop.framework.autoproxy.AutoProxyUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClientFactoryBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 内部 HTTP 远程调用配置.
 *
 * 这里把运行时几条链路接起来:
 * 1. Consumer 发请求前透传认证头和 Seata XID
 * 2. 远程非 2xx 响应统一转成 ServiceException
 * 3. 打开请求日志时，为 consumer/provider 两侧挂统一日志
 *
 * @author Lion Li
 */
@Slf4j
@AutoConfiguration
@EnableFeignClients(basePackages = "org.dromara")
public class RemoteHttpAutoConfiguration {

    @Bean
    public static BeanDefinitionRegistryPostProcessor remoteHttpControllerProxyCompatibilityPostProcessor() {
        return new RemoteHttpInfrastructurePostProcessor();
    }

    @Bean
    public RequestInterceptor remoteHttpRequestInterceptor() {
        return requestTemplate -> {
            HttpHeaders headers = new HttpHeaders();
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
            headers.forEach((key, values) -> values.forEach(value -> requestTemplate.header(key, value)));
        };
    }

    @Bean
    public Logger remoteHttpFeignLogger(
        RemoteHttpLogSupport remoteHttpLogSupport) {
        return new RemoteHttpFeignLogger(remoteHttpLogSupport);
    }

    @Bean
    public Logger.Level remoteHttpFeignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public ErrorDecoder remoteHttpErrorDecoder() {
        return (methodKey, response) -> buildServiceException(response.status(), response.reason(), readResponseBody(response));
    }

    @Bean
    public RemoteHttpLogSupport remoteHttpLogSupport() {
        return new RemoteHttpLogSupport();
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

    private String readResponseBody(Response response) {
        if (response.body() == null) {
            return null;
        }
        try {
            return StreamUtils.copyToString(response.body().asInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.debug("read remote response body failed", e);
            return null;
        }
    }

    private ServiceException buildServiceException(int statusCode, String statusText, String responseBody) {
        if (StringUtils.isNotBlank(responseBody) && JsonUtils.isJsonObject(responseBody)) {
            try {
                // 远程服务如果按 R 返回错误信息，优先还原成更友好的业务异常消息。
                R<?> result = JsonUtils.parseObject(responseBody, R.class);
                if (result != null && (result.getCode() == 0 || R.isSuccess(result))) {
                    return new ServiceException(StringUtils.defaultIfBlank(statusText, "远程服务调用失败"), statusCode);
                }
                if (result != null && StringUtils.isNotBlank(result.getMsg())) {
                    return new ServiceException(result.getMsg(), result.getCode());
                }
            } catch (RuntimeException e) {
                log.debug("parse remote error body failed: {}", responseBody, e);
            }
        }
        String message = StringUtils.firstNonBlank(responseBody, statusText, "远程服务调用失败");
        return new ServiceException(message, statusCode);
    }

    private static final class RemoteHttpInfrastructurePostProcessor implements BeanDefinitionRegistryPostProcessor {

        @Override
        public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
            for (String beanName : registry.getBeanDefinitionNames()) {
                BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
                registerFallbackBeanIfNecessary(registry, beanDefinition, "fallback");
                registerFallbackBeanIfNecessary(registry, beanDefinition, "fallbackFactory");
            }
        }

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
            for (String beanName : beanFactory.getBeanDefinitionNames()) {
                BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
                preserveRemoteControllerTargetClass(beanDefinition);
            }
        }

        private void registerFallbackBeanIfNecessary(BeanDefinitionRegistry registry, BeanDefinition beanDefinition,
            String propertyName) {
            if (!FeignClientFactoryBean.class.getName().equals(beanDefinition.getBeanClassName())) {
                return;
            }
            Object propertyValue = beanDefinition.getPropertyValues().get(propertyName);
            if (!(propertyValue instanceof Class<?> fallbackType) || Object.class == fallbackType || void.class == fallbackType) {
                return;
            }
            // fallback/fallbackFactory 常放在 api 模块里，默认应用包扫描不会覆盖到这里，
            // 所以在 FeignClient 注册完成后顺手把它补成 Spring Bean。
            String fallbackBeanName = fallbackType.getName();
            if (registry.containsBeanDefinition(fallbackBeanName) || hasBeanClass(registry, fallbackType)) {
                return;
            }
            registry.registerBeanDefinition(fallbackBeanName,
                BeanDefinitionBuilder.genericBeanDefinition(fallbackType).getBeanDefinition());
        }

        private boolean hasBeanClass(BeanDefinitionRegistry registry, Class<?> beanClass) {
            for (String beanName : registry.getBeanDefinitionNames()) {
                BeanDefinition candidate = registry.getBeanDefinition(beanName);
                if (beanClass.getName().equals(candidate.getBeanClassName())) {
                    return true;
                }
            }
            return false;
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
    }
}
