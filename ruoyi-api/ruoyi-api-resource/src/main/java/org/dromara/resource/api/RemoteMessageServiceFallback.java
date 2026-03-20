package org.dromara.resource.api;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 消息服务熔断降级.
 *
 * @author Lion Li
 */
@Slf4j
public class RemoteMessageServiceFallback implements RemoteMessageService {

    /**
     * 发送消息
     *
     * @param sessionKey session主键 一般为用户id
     * @param message    消息文本
     */
    @Override
    public void publishMessage(List<Long> sessionKey, String message) {
        log.warn("消息服务调用失败, 已触发熔断降级");
    }

    /**
     * 发布订阅的消息(群发)
     *
     * @param message 消息内容
     */
    @Override
    public void publishAll(String message) {
        log.warn("消息服务调用失败, 已触发熔断降级");
    }
}
