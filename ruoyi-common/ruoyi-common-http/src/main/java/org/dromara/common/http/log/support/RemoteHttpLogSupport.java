package org.dromara.common.http.log.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;

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
public class RemoteHttpLogSupport {

    public static final String CONSUMER = "CONSUMER";
    public static final String PROVIDER = "PROVIDER";

    public void logRequest(String client, HttpMethod httpMethod, String path, Object[] arguments) {
        String baseLog = buildBaseLog(client, httpMethod, path);
        log.info("HTTP - 服务调用: {}", baseLog);
    }

    public void logResponse(String client, HttpMethod httpMethod, String path, long elapsed, Object response) {
        String baseLog = buildBaseLog(client, httpMethod, path);
        log.info("HTTP - 服务响应: {},SpendTime=[{}ms]", baseLog, elapsed);
    }

    public void logException(String client, HttpMethod httpMethod, String path, long elapsed, Throwable throwable) {
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

}
