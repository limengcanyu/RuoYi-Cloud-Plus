package org.dromara.common.http.log.support;

import feign.Logger;
import feign.Request;
import feign.Response;
import feign.Util;
import org.springframework.http.HttpMethod;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Feign 原生日志桥接。
 */
public class RemoteHttpFeignLogger extends Logger {

    private final RemoteHttpLogSupport logSupport;

    public RemoteHttpFeignLogger(RemoteHttpLogSupport logSupport) {
        this.logSupport = logSupport;
    }

    @Override
    protected void log(String configKey, String format, Object... args) {
    }

    @Override
    protected void logRequest(String configKey, Level logLevel, Request request) {
        Object[] arguments = request.body() == null ? new Object[0] : new Object[] {request.body()};
        this.logSupport.logRequest(RemoteHttpLogSupport.CONSUMER, HttpMethod.valueOf(request.httpMethod().name()), request.url(), arguments);
    }

    @Override
    protected Response logAndRebufferResponse(String configKey, Level logLevel, Response response, long elapsedTime)
        throws IOException {
        byte[] bodyData = response.body() == null ? null : Util.toByteArray(response.body().asInputStream());
        Object responseBody = bodyData == null ? null : new String(bodyData, StandardCharsets.UTF_8);
        HttpMethod httpMethod = response.request() == null ? null : HttpMethod.valueOf(response.request().httpMethod().name());
        String path = response.request() == null ? null : response.request().url();
        this.logSupport.logResponse(RemoteHttpLogSupport.CONSUMER, httpMethod, path, elapsedTime, responseBody);
        if (bodyData == null) {
            return response;
        }
        return response.toBuilder().body(bodyData).build();
    }

    @Override
    protected IOException logIOException(String configKey, Level logLevel, IOException ioe, long elapsedTime) {
        this.logSupport.logException(RemoteHttpLogSupport.CONSUMER, null, configKey, elapsedTime, ioe);
        return ioe;
    }
}
