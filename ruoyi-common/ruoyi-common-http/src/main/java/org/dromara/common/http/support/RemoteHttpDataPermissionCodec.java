package org.dromara.common.http.support;

import org.apache.fory.Fory;
import org.apache.fory.ThreadSafeFory;
import org.apache.fory.config.Language;
import org.dromara.common.core.exception.ServiceException;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 数据权限上下文编解码器.
 *
 * 通过 Fory + Base64 把上下文压缩成单个 HTTP Header，避免手工维护复杂类型转换。
 */
public class RemoteHttpDataPermissionCodec {

    public static final String HEADER_NAME = "X-Remote-Data-Permission";

    private static final int MAX_HEADER_LENGTH = 6 * 1024;

    private static final ThreadSafeFory FORY = Fory.builder()
        .withLanguage(Language.JAVA)
        .withRefTracking(true)
        .withStringCompressed(true)
        .withNumberCompressed(true)
        .requireClassRegistration(false)
        .buildThreadSafeFory();

    public String encode(Map<String, Object> context) {
        if (context == null || context.isEmpty()) {
            return null;
        }
        try {
            byte[] bytes = FORY.serialize(context);
            String headerValue = Base64.getEncoder().encodeToString(bytes);
            if (headerValue.length() > MAX_HEADER_LENGTH) {
                throw new ServiceException("数据权限上下文过大，无法透传");
            }
            return headerValue;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("数据权限上下文序列化失败");
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> decode(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            return Map.of();
        }
        try {
            byte[] bytes = Base64.getDecoder().decode(headerValue);
            Object value = FORY.deserialize(bytes);
            if (value instanceof Map<?, ?> map) {
                return new LinkedHashMap<>((Map<String, Object>) map);
            }
        } catch (Exception e) {
            throw new ServiceException("数据权限上下文反序列化失败");
        }
        throw new ServiceException("数据权限上下文格式非法");
    }
}
