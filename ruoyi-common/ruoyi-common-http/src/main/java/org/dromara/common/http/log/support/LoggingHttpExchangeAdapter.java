package org.dromara.common.http.log.support;

import org.dromara.common.core.exception.ServiceException;
import org.jspecify.annotations.Nullable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.service.invoker.HttpExchangeAdapter;
import org.springframework.web.service.invoker.HttpExchangeAdapterDecorator;
import org.springframework.web.service.invoker.HttpRequestValues;

import java.net.URI;
import java.util.Map;

/**
 * 内部 HTTP Consumer 日志装饰器.
 *
 * Consumer 侧日志挂在 HttpServiceProxyFactory 的 exchange adapter 上，
 * 这样可以直接拿到最终请求 method/path 和解码后的返回值，
 * 比直接拦截底层流更稳定，也更容易规避 body 重复读问题。
 *
 * @author Lion Li
 */
public class LoggingHttpExchangeAdapter extends HttpExchangeAdapterDecorator {

    private final RemoteHttpLogSupport logSupport;

    public LoggingHttpExchangeAdapter(HttpExchangeAdapter delegate, RemoteHttpLogSupport logSupport) {
        super(delegate);
        this.logSupport = logSupport;
    }

    @Override
    public void exchange(HttpRequestValues requestValues) {
        invoke(requestValues, () -> {
            super.exchange(requestValues);
            return null;
        });
    }

    @Override
    public HttpHeaders exchangeForHeaders(HttpRequestValues requestValues) {
        return invoke(requestValues, () -> super.exchangeForHeaders(requestValues));
    }

    @Override
    public <T> @Nullable T exchangeForBody(HttpRequestValues requestValues, ParameterizedTypeReference<T> bodyType) {
        return invoke(requestValues, () -> super.exchangeForBody(requestValues, bodyType));
    }

    @Override
    public ResponseEntity<Void> exchangeForBodilessEntity(HttpRequestValues requestValues) {
        return invoke(requestValues, () -> super.exchangeForBodilessEntity(requestValues));
    }

    @Override
    public <T> ResponseEntity<T> exchangeForEntity(HttpRequestValues requestValues, ParameterizedTypeReference<T> bodyType) {
        return invoke(requestValues, () -> super.exchangeForEntity(requestValues, bodyType));
    }

    private <T> T invoke(HttpRequestValues requestValues, ThrowingSupplier<T> supplier) {
        HttpMethod httpMethod = requestValues.getHttpMethod();
        String path = resolvePath(requestValues);
        Object bodyValue = requestValues.getBodyValue();
        Object[] arguments = bodyValue == null ? new Object[0] : bodyValue instanceof Object[] array ? array : new Object[] {bodyValue};
        this.logSupport.logRequest(RemoteHttpLogSupport.CONSUMER, httpMethod, path, arguments);
        long startTime = System.currentTimeMillis();
        try {
            T result = supplier.get();
            this.logSupport.logResponse(RemoteHttpLogSupport.CONSUMER, httpMethod, path,
                System.currentTimeMillis() - startTime, result);
            return result;
        } catch (Throwable ex) {
            this.logSupport.logException(RemoteHttpLogSupport.CONSUMER, httpMethod, path,
                System.currentTimeMillis() - startTime, ex);
            switch (ex) {
                case ServiceException serviceException -> throw serviceException;
                case RuntimeException runtimeException -> throw runtimeException;
                case Error error -> throw error;
                default -> {
                }
            }
            throw new IllegalStateException(ex);
        }
    }

    private String resolvePath(HttpRequestValues requestValues) {
        URI uri = requestValues.getUri();
        if (uri != null) {
            // 能拿到最终 URI 时优先打印最终请求地址，便于线上排查。
            return uri.toString();
        }
        String uriTemplate = requestValues.getUriTemplate();
        if (!StringUtils.hasText(uriTemplate)) {
            return null;
        }
        Map<String, String> uriVariables = requestValues.getUriVariables();
        String path = uriTemplate;
        if (uriVariables != null) {
            for (Map.Entry<String, String> entry : uriVariables.entrySet()) {
                path = path.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
            }
        }
        return path;
    }

    @FunctionalInterface
    private interface ThrowingSupplier<T> {

        T get() throws Throwable;
    }
}
