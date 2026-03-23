package org.dromara.resource.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 消息服务
 *
 * @author Lion Li
 */
@FeignClient(contextId = "remoteMessageService", name = "ruoyi-resource", path = "/remote/message",
    fallbackFactory = RemoteMessageServiceFallbackFactory.class, primary = false)
public interface RemoteMessageService {

    /**
     * 发送消息
     *
     * @param sessionKey session主键 一般为用户id
     * @param message    消息文本
     */
    @PostMapping("/publish-message")
    void publishMessage(@RequestBody List<Long> sessionKey, @RequestParam String message);

    /**
     * 发布订阅的消息(群发)
     *
     * @param message 消息内容
     */
    @PostMapping("/publish-all")
    void publishAll(@RequestParam String message);
}

