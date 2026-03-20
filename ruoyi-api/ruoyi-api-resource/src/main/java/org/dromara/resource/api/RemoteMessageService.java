package org.dromara.resource.api;

import org.dromara.common.core.annotation.RemoteHttpService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

/**
 * 消息服务
 *
 * @author Lion Li
 */
@RemoteHttpService(value = "ruoyi-resource", fallback = RemoteMessageServiceFallback.class)
@HttpExchange("/remote/message")
public interface RemoteMessageService {

    /**
     * 发送消息
     *
     * @param sessionKey session主键 一般为用户id
     * @param message    消息文本
     */
    @PostExchange("/publish-message")
    void publishMessage(@RequestBody List<Long> sessionKey, @RequestParam String message);

    /**
     * 发布订阅的消息(群发)
     *
     * @param message 消息内容
     */
    @PostExchange("/publish-all")
    void publishAll(@RequestParam String message);
}
