package org.dromara.common.http.support;

import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * consumer 侧数据权限透传拦截器。
 */
public class RemoteHttpDataPermissionRequestInterceptor implements RequestInterceptor {

    private final RemoteHttpDataPermissionCodec codec;

    public RemoteHttpDataPermissionRequestInterceptor(RemoteHttpDataPermissionCodec codec) {
        this.codec = codec;
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        if (!RemoteHttpDataPermissionSupport.hasContext()) {
            return;
        }
        String headerValue = this.codec.encode(RemoteHttpDataPermissionSupport.snapshotContext());
        if (headerValue != null) {
            requestTemplate.header(RemoteHttpDataPermissionCodec.HEADER_NAME, headerValue);
        }
    }
}
