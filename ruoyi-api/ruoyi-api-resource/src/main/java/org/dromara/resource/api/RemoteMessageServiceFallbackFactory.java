package org.dromara.resource.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.List;

/**
 * 消息服务 fallback factory.
 *
 * @author Lion Li
 */
@Slf4j
public class RemoteMessageServiceFallbackFactory implements FallbackFactory<RemoteMessageService> {

    @Override
    public RemoteMessageService create(Throwable cause) {
        return new RemoteMessageService() {
            @Override
            public void publishMessage(List<Long> sessionKey, String message) {
                log.warn("消息服务调用失败, 已触发 fallback", cause);
            }

            @Override
            public void publishAll(String message) {
                log.warn("消息服务调用失败, 已触发 fallback", cause);
            }
        };
    }
}
