package org.dromara.common.http.log.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.cloud.openfeign.FeignClient;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.dromara.common.core.utils.ServletUtils;
import org.dromara.common.http.log.support.RemoteHttpLogSupport;
import org.springframework.http.HttpMethod;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;

/**
 * 内部 HTTP Provider 日志切面.
 *
 * Provider 侧日志不直接读原始请求 body，而是等 Spring 完成参数绑定后
 * 直接记录方法入参/返回值，这样可以避免 servlet body 重复读取。
 *
 * @author Lion Li
 */
@Aspect
@RequiredArgsConstructor
public class RemoteHttpProviderLogAspect {

    private final RemoteHttpLogSupport logSupport;

    @Around("@within(org.dromara.common.http.annotation.RemoteServiceController) && execution(public * *(..))")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = AopUtils.getTargetClass(joinPoint.getTarget());
        Object[] arguments = joinPoint.getArgs();
        HttpServletRequest request = ServletUtils.getRequest();
        Class<?> remoteInterface = resolveRemoteInterface(targetClass, method);
        // 真实 HTTP 调用时优先从 servlet 请求拿 method/path；
        // 本地注入 provider bean 时再回退到接口上的 Spring MVC 映射注解。
        HttpMethod httpMethod = resolveHttpMethod(request, remoteInterface, targetClass, method);
        String path = resolvePath(request, remoteInterface, targetClass, method);
        this.logSupport.logRequest(RemoteHttpLogSupport.PROVIDER, httpMethod, path, arguments);
        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            this.logSupport.logResponse(RemoteHttpLogSupport.PROVIDER, httpMethod, path, System.currentTimeMillis() - startTime, result);
            return result;
        } catch (Throwable ex) {
            this.logSupport.logException(RemoteHttpLogSupport.PROVIDER, httpMethod, path, System.currentTimeMillis() - startTime, ex);
            throw ex;
        }
    }

    private HttpMethod resolveHttpMethod(HttpServletRequest request, Class<?> remoteInterface, Class<?> targetClass, Method method) {
        if (request != null && StringUtils.hasText(request.getMethod())) {
            return HttpMethod.valueOf(request.getMethod());
        }
        RequestMapping methodMapping = resolveMethodMapping(remoteInterface, method);
        if (methodMapping != null && methodMapping.method().length > 0) {
            return HttpMethod.valueOf(methodMapping.method()[0].name());
        }
        RequestMapping typeMapping = resolveTypeMapping(remoteInterface);
        if (typeMapping == null) {
            typeMapping = resolveTypeMapping(targetClass);
        }
        if (typeMapping != null && typeMapping.method().length > 0) {
            return HttpMethod.valueOf(typeMapping.method()[0].name());
        }
        return null;
    }

    private String resolvePath(HttpServletRequest request, Class<?> remoteInterface, Class<?> targetClass, Method method) {
        if (request != null) {
            String requestUri = request.getRequestURI();
            if (StringUtils.hasText(requestUri)) {
                String queryString = request.getQueryString();
                if (!StringUtils.hasText(queryString)) {
                    return requestUri;
                }
                return requestUri + '?' + queryString;
            }
        }
        String typePath = extractPath(resolveTypeMapping(remoteInterface));
        if (!StringUtils.hasText(typePath)) {
            typePath = extractPath(resolveTypeMapping(targetClass));
        }
        String methodPath = extractPath(resolveMethodMapping(remoteInterface, method));
        if (!StringUtils.hasText(typePath)) {
            return methodPath;
        }
        if (!StringUtils.hasText(methodPath)) {
            return typePath;
        }
        // 拼出接口级 + 方法级路径，作为本地短路场景下的日志定位信息。
        return combinePath(typePath, methodPath);
    }

    private Class<?> resolveRemoteInterface(Class<?> targetClass, Method method) {
        for (Class<?> interfaceType : targetClass.getInterfaces()) {
            if (interfaceType.isAnnotationPresent(FeignClient.class)
                && org.springframework.util.ReflectionUtils.findMethod(interfaceType, method.getName(), method.getParameterTypes()) != null) {
                return interfaceType;
            }
        }
        return null;
    }

    private RequestMapping resolveTypeMapping(Class<?> remoteInterface) {
        if (remoteInterface == null) {
            return null;
        }
        return AnnotatedElementUtils.findMergedAnnotation(remoteInterface, RequestMapping.class);
    }

    private RequestMapping resolveMethodMapping(Class<?> remoteInterface, Method method) {
        if (remoteInterface == null) {
            return null;
        }
        Method interfaceMethod = org.springframework.util.ReflectionUtils.findMethod(remoteInterface, method.getName(), method.getParameterTypes());
        if (interfaceMethod == null) {
            return null;
        }
        return AnnotatedElementUtils.findMergedAnnotation(interfaceMethod, RequestMapping.class);
    }

    private String extractPath(RequestMapping mapping) {
        if (mapping == null) {
            return null;
        }
        if (mapping.path().length > 0 && StringUtils.hasText(mapping.path()[0])) {
            return mapping.path()[0];
        }
        if (mapping.value().length > 0 && StringUtils.hasText(mapping.value()[0])) {
            return mapping.value()[0];
        }
        return null;
    }

    private String combinePath(String typePath, String methodPath) {
        String normalizedTypePath = trimTrailingSlash(typePath);
        String normalizedMethodPath = trimLeadingSlash(methodPath);
        if (!StringUtils.hasText(normalizedTypePath)) {
            return '/' + normalizedMethodPath;
        }
        if (!StringUtils.hasText(normalizedMethodPath)) {
            return normalizedTypePath;
        }
        return normalizedTypePath + '/' + normalizedMethodPath;
    }

    private String trimTrailingSlash(String path) {
        if (!StringUtils.hasText(path)) {
            return path;
        }
        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

    private String trimLeadingSlash(String path) {
        if (!StringUtils.hasText(path)) {
            return path;
        }
        return path.startsWith("/") ? path.substring(1) : path;
    }
}
