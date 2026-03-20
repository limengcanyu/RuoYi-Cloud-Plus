package org.dromara.common.http.support;

import org.aopalliance.intercept.MethodInterceptor;
import org.dromara.common.core.annotation.RemoteHttpService;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;

/**
 * 远程 HTTP 代理 fallback 包装器.
 *
 * <p>仅包装注册器生成的远程 HTTP 代理 Bean。代理调用报错时，
 * 按接口上声明的 fallback 实现兜底，不处理本地 provider Bean。
 *
 * <p>这里故意保持和之前 mock/stub 类似的简单约束：
 * fallback 必须实现接口本身，且方法签名与接口保持一致。</p>
 *
 * @author Lion Li
 */
public class RemoteHttpFallbackProxyPostProcessor
    implements BeanPostProcessor, BeanFactoryAware, BeanClassLoaderAware {

    private static final String HTTP_SERVICE_GROUP_NAME_ATTRIBUTE = "httpServiceGroupName";
    private static final String FALLBACK_WRAPPED_ATTRIBUTE = "remoteHttpFallbackWrapped";

    private ConfigurableListableBeanFactory beanFactory;
    private ClassLoader beanClassLoader;

    @Override
    public void setBeanFactory(org.springframework.beans.factory.BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.beanClassLoader = classLoader;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof FallbackDecoratedProxy) {
            return bean;
        }
        Class<?> serviceInterface = resolveRemoteServiceInterface(beanName, bean);
        if (serviceInterface == null) {
            return bean;
        }
        RemoteHttpService remoteHttpService = serviceInterface.getAnnotation(RemoteHttpService.class);
        if (remoteHttpService == null || remoteHttpService.fallback() == void.class) {
            return bean;
        }
        Class<?> fallbackClass = remoteHttpService.fallback();
        if (!serviceInterface.isAssignableFrom(fallbackClass)) {
            throw new IllegalStateException("Fallback class must implement remote service interface: "
                + fallbackClass.getName() + " -> " + serviceInterface.getName());
        }
        ProxyFactory proxyFactory = new ProxyFactory(bean);
        proxyFactory.setInterfaces(ClassUtils.getAllInterfacesForClass(bean.getClass(), this.beanClassLoader));
        proxyFactory.addInterface(FallbackDecoratedProxy.class);
        proxyFactory.addAdvice((MethodInterceptor) invocation -> {
            Method method = invocation.getMethod();
            if (method.getDeclaringClass() == Object.class) {
                return invocation.proceed();
            }
            try {
                return invocation.proceed();
            } catch (Throwable ex) {
                return invokeFallback(serviceInterface, fallbackClass, method, invocation.getArguments(), ex);
            }
        });
        markWrapped(beanName);
        return proxyFactory.getProxy(this.beanClassLoader);
    }

    private Class<?> resolveRemoteServiceInterface(String beanName, Object bean) {
        if (this.beanFactory == null || !this.beanFactory.containsBeanDefinition(beanName)) {
            return null;
        }
        BeanDefinition beanDefinition = this.beanFactory.getBeanDefinition(beanName);
        if (beanDefinition.getAttribute(HTTP_SERVICE_GROUP_NAME_ATTRIBUTE) == null) {
            return null;
        }
        if (Boolean.TRUE.equals(beanDefinition.getAttribute(FALLBACK_WRAPPED_ATTRIBUTE))) {
            return null;
        }
        Class<?> beanClass = resolveBeanClass(beanDefinition);
        if (beanClass != null && beanClass.isInterface() && beanClass.isAnnotationPresent(RemoteHttpService.class)) {
            return beanClass;
        }
        for (Class<?> interfaceType : ClassUtils.getAllInterfacesForClass(bean.getClass(), this.beanClassLoader)) {
            if (interfaceType.isAnnotationPresent(RemoteHttpService.class)) {
                return interfaceType;
            }
        }
        return null;
    }

    private Class<?> resolveBeanClass(BeanDefinition beanDefinition) {
        String beanClassName = beanDefinition.getBeanClassName();
        return beanClassName == null ? null : ClassUtils.resolveClassName(beanClassName, this.beanClassLoader);
    }

    private Object invokeFallback(Class<?> serviceInterface, Class<?> fallbackClass, Method method, Object[] args, Throwable ex)
        throws Throwable {
        Object fallbackInstance = instantiateFallback(fallbackClass);
        Method fallbackMethod = ReflectionUtils.findMethod(fallbackClass, method.getName(), method.getParameterTypes());
        if (fallbackMethod == null) {
            throw unwrap(ex);
        }
        ReflectionUtils.makeAccessible(fallbackMethod);
        return invokeMethod(fallbackInstance, fallbackMethod, args);
    }

    private Object instantiateFallback(Class<?> fallbackClass) {
        if (this.beanFactory == null) {
            throw new IllegalStateException("BeanFactory not initialized for remote fallback: " + fallbackClass.getName());
        }
        return this.beanFactory.getBean(fallbackClass);
    }

    private void markWrapped(String beanName) {
        if (this.beanFactory == null || !this.beanFactory.containsBeanDefinition(beanName)) {
            return;
        }
        this.beanFactory.getBeanDefinition(beanName).setAttribute(FALLBACK_WRAPPED_ATTRIBUTE, true);
    }

    private Object invokeMethod(Object target, Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException ex) {
            throw unwrap(ex.getTargetException());
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException("Could not invoke remote fallback method: " + method, ex);
        } catch (UndeclaredThrowableException ex) {
            throw unwrap(ex);
        } catch (RuntimeException ex) {
            throw unwrap(ex);
        }
    }

    private Throwable unwrap(Throwable throwable) {
        Throwable current = throwable;
        while (current instanceof InvocationTargetException invocationTargetException && invocationTargetException.getTargetException() != null) {
            current = invocationTargetException.getTargetException();
        }
        while (current instanceof UndeclaredThrowableException undeclaredThrowableException && undeclaredThrowableException.getUndeclaredThrowable() != null) {
            current = undeclaredThrowableException.getUndeclaredThrowable();
        }
        return current;
    }

    private interface FallbackDecoratedProxy {
    }
}
