package org.dromara.common.http.log.support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.http.log.enums.RequestLogEnum;
import org.dromara.common.http.properties.RemoteHttpProperties;
import org.dromara.common.json.utils.JsonUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 内部 HTTP 日志支持.
 *
 * 这里只做两件事:
 * 1. 统一 consumer/provider 的日志格式
 * 2. 对 byte[] 等内容做简单脱敏，避免日志直接刷大块二进制
 *
 * @author Lion Li
 */
@Slf4j
@RequiredArgsConstructor
public class RemoteHttpLogSupport {

    public static final String CONSUMER = "CONSUMER";
    public static final String PROVIDER = "PROVIDER";

    private final RemoteHttpProperties properties;

    public boolean isEnabled() {
        return Boolean.TRUE.equals(properties.getRequestLog());
    }

    public boolean isFullLogEnabled() {
        return properties.getLogLevel() == RequestLogEnum.FULL;
    }

    public void logRequest(String client, HttpMethod httpMethod, String path, Object[] arguments) {
        if (!isEnabled()) {
            return;
        }
        String baseLog = buildBaseLog(client, httpMethod, path);
        if (properties.getLogLevel() == RequestLogEnum.INFO) {
            log.info("HTTP - 服务调用: {}", baseLog);
            return;
        }
        log.info("HTTP - 服务调用: {},Parameter={}", baseLog, formatArguments(arguments));
    }

    public void logResponse(String client, HttpMethod httpMethod, String path, long elapsed, Object response) {
        if (!isEnabled()) {
            return;
        }
        String baseLog = buildBaseLog(client, httpMethod, path);
        if (properties.getLogLevel() == RequestLogEnum.FULL) {
            log.info("HTTP - 服务响应: {},SpendTime=[{}ms],Response={}", baseLog, elapsed, formatValue(unwrapResponse(response)));
            return;
        }
        log.info("HTTP - 服务响应: {},SpendTime=[{}ms]", baseLog, elapsed);
    }

    public void logException(String client, HttpMethod httpMethod, String path, long elapsed, Throwable throwable) {
        if (!isEnabled()) {
            return;
        }
        String baseLog = buildBaseLog(client, httpMethod, path);
        log.error("HTTP - 服务异常: {},SpendTime=[{}ms],Exception={}", baseLog, elapsed, throwable.getMessage(), throwable);
    }

    private String buildBaseLog(String client, HttpMethod httpMethod, String path) {
        return "Client[" + client + ']' +
            ",HttpMethod[" +
            (httpMethod != null ? httpMethod : "UNKNOWN") +
            ']' +
            ",Path[" +
            (StringUtils.hasText(path) ? path : "UNKNOWN") +
            ']';
    }

    private String formatArguments(Object[] arguments) {
        return formatValue(arguments == null ? new Object[0] : arguments);
    }

    private Object unwrapResponse(Object response) {
        if (response instanceof ResponseEntity<?> responseEntity) {
            return responseEntity.getBody();
        }
        return response;
    }

    private String formatValue(Object value) {
        try {
            return JsonUtils.toJsonString(sanitizeValue(value));
        } catch (RuntimeException ignored) {
            return String.valueOf(value);
        }
    }

    private Object sanitizeValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof byte[] bytes) {
            // 文件上传这类场景只记录长度，避免二进制内容直接进日志。
            return "byte[" + bytes.length + "]";
        }
        if (value instanceof Object[] array) {
            Object[] sanitized = new Object[array.length];
            for (int i = 0; i < array.length; i++) {
                sanitized[i] = sanitizeValue(array[i]);
            }
            return sanitized;
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream().map(this::sanitizeValue).toList();
        }
        if (value instanceof Map<?, ?> map) {
            Map<Object, Object> sanitized = new LinkedHashMap<>(map.size());
            map.forEach((key, item) -> sanitized.put(key, sanitizeValue(item)));
            return sanitized;
        }
        if (ObjectUtils.isArray(value)) {
            return ObjectUtils.nullSafeToString(value);
        }
        return value;
    }
}
