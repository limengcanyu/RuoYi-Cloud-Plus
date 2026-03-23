package org.dromara.common.http.support;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

import java.util.Map;

/**
 * provider 侧恢复数据权限上下文，并在请求结束后清理，避免线程复用污染。
 */
public class RemoteHttpDataPermissionInterceptor implements AsyncHandlerInterceptor {

    private static final String CONTEXT_ATTRIBUTE = RemoteHttpDataPermissionInterceptor.class.getName() + ".context";

    private final RemoteHttpDataPermissionCodec codec;

    public RemoteHttpDataPermissionInterceptor(RemoteHttpDataPermissionCodec codec) {
        this.codec = codec;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String headerValue = request.getHeader(RemoteHttpDataPermissionCodec.HEADER_NAME);
        if (StringUtils.isBlank(headerValue)) {
            return true;
        }
        request.setAttribute(CONTEXT_ATTRIBUTE, RemoteHttpDataPermissionSupport.snapshotContext());
        RemoteHttpDataPermissionSupport.replaceContext(this.codec.decode(headerValue));
        return true;
    }

    @Override
    public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response, Object handler) {
        restoreContext(request);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        restoreContext(request);
    }

    @SuppressWarnings("unchecked")
    private void restoreContext(HttpServletRequest request) {
        Object context = request.getAttribute(CONTEXT_ATTRIBUTE);
        if (!(context instanceof Map<?, ?> previousContext)) {
            return;
        }
        RemoteHttpDataPermissionSupport.replaceContext((Map<String, Object>) previousContext);
        request.removeAttribute(CONTEXT_ATTRIBUTE);
    }
}
