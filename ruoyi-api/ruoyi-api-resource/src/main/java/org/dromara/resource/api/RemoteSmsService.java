package org.dromara.resource.api;

import org.dromara.common.core.annotation.RemoteHttpService;
import org.dromara.resource.api.domain.RemoteSms;
import org.dromara.resource.api.domain.RemoteSmsBatch;
import org.dromara.resource.api.domain.RemoteSmsDelayBatch;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * 短信服务
 *
 * @author Feng
 */
@RemoteHttpService("ruoyi-resource")
@HttpExchange("/inner/remote/resource/sms")
public interface RemoteSmsService {

    /**
     * 同步方法：发送固定消息模板短信
     *
     * @param phone   目标手机号
     * @param message 短信内容
     * @return 封装了短信发送结果的 RemoteSms 对象
     */
    @PostExchange("/send-text")
    RemoteSms sendMessage(@RequestParam String phone, @RequestParam String message);

    /**
     * 同步方法：发送固定消息模板多模板参数短信
     *
     * @param phone    目标手机号
     * @param messages 短信模板参数，使用 LinkedHashMap 以保持参数顺序
     * @return 封装了短信发送结果的 RemoteSms 对象
     */
    @PostExchange("/send-vars")
    RemoteSms sendMessage(@RequestParam String phone, @RequestBody LinkedHashMap<String, String> messages);

    /**
     * 同步方法：使用自定义模板发送短信
     *
     * @param phone      目标手机号
     * @param templateId 短信模板ID
     * @param messages   短信模板参数，使用 LinkedHashMap 以保持参数顺序
     * @return 封装了短信发送结果的 RemoteSms 对象
     */
    @PostExchange("/send-template")
    RemoteSms sendMessage(@RequestParam String phone, @RequestParam String templateId,
                          @RequestBody LinkedHashMap<String, String> messages);

    /**
     * 同步方法：群发固定模板短信
     *
     * @param phones  目标手机号列表（1~1000）
     * @param message 短信内容
     * @return 封装了短信发送结果的 RemoteSms 对象
     */
    @PostExchange("/message-texting")
    RemoteSms messageTexting(@RequestBody List<String> phones, @RequestParam String message);

    /**
     * 同步方法：使用自定义模板群发短信
     *
     * @param phones     目标手机号列表（1~1000）（1~1000）
     * @param templateId 短信模板ID
     * @param messages   短信模板参数，使用 LinkedHashMap 以保持参数顺序
     * @return 封装了短信发送结果的 RemoteSms 对象
     */
    @PostExchange("/message-texting-template")
    default RemoteSms messageTexting(List<String> phones, String templateId, LinkedHashMap<String, String> messages) {
        return messageTextingTemplate(new RemoteSmsBatch(phones, templateId, messages));
    }

    /**
     * 使用自定义模板群发短信.
     *
     * @param request 群发模板短信请求
     * @return 封装了短信发送结果的 RemoteSms 对象
     */
    @PostExchange("/message-texting-template")
    RemoteSms messageTextingTemplate(@RequestBody RemoteSmsBatch request);

    /**
     * 异步方法：发送固定消息模板短信
     *
     * @param phone   目标手机号
     * @param message 短信内容
     */
    @PostExchange("/send-async-text")
    void sendMessageAsync(@RequestParam String phone, @RequestParam String message);

    /**
     * 异步方法：使用自定义模板发送短信
     *
     * @param phone      目标手机号
     * @param templateId 短信模板ID
     * @param messages   短信模板参数，使用 LinkedHashMap 以保持参数顺序
     */
    @PostExchange("/send-async-template")
    void sendMessageAsync(@RequestParam String phone, @RequestParam String templateId,
                          @RequestBody LinkedHashMap<String, String> messages);

    /**
     * 延迟发送：发送固定消息模板短信
     *
     * @param phone       目标手机号
     * @param message     短信内容
     * @param delayedTime 延迟发送时间（毫秒）
     */
    @PostExchange("/delay-text")
    void delayMessage(@RequestParam String phone, @RequestParam String message, @RequestParam Long delayedTime);

    /**
     * 延迟发送：使用自定义模板发送定时短信
     *
     * @param phone       目标手机号
     * @param templateId  短信模板ID
     * @param messages    短信模板参数，使用 LinkedHashMap 以保持参数顺序
     * @param delayedTime 延迟发送时间（毫秒）
     */
    @PostExchange("/delay-template")
    void delayMessage(@RequestParam String phone, @RequestParam String templateId,
                      @RequestBody LinkedHashMap<String, String> messages, @RequestParam Long delayedTime);

    /**
     * 延迟群发：群发延迟短信
     *
     * @param phones      目标手机号列表（1~1000）
     * @param message     短信内容
     * @param delayedTime 延迟发送时间（毫秒）
     */
    @PostExchange("/delay-message-texting")
    void delayMessageTexting(@RequestBody List<String> phones, @RequestParam String message, @RequestParam Long delayedTime);

    /**
     * 延迟群发：使用自定义模板发送群体延迟短信
     *
     * @param phones      目标手机号列表（1~1000）
     * @param templateId  短信模板ID
     * @param messages    短信模板参数，使用 LinkedHashMap 以保持参数顺序
     * @param delayedTime 延迟发送时间（毫秒）
     */
    @PostExchange("/delay-message-texting-template")
    default void delayMessageTexting(List<String> phones, String templateId,
                                     LinkedHashMap<String, String> messages, Long delayedTime) {
        delayMessageTextingTemplate(new RemoteSmsDelayBatch(phones, templateId, messages, delayedTime));
    }

    /**
     * 延迟群发模板短信.
     *
     * @param request 延迟群发模板短信请求
     */
    @PostExchange("/delay-message-texting-template")
    void delayMessageTextingTemplate(@RequestBody RemoteSmsDelayBatch request);

    /**
     * 加入黑名单
     *
     * @param phone 手机号
     */
    @PostExchange("/add-blacklist-one")
    void addBlacklist(@RequestParam String phone);

    /**
     * 加入黑名单
     *
     * @param phones 手机号列表
     */
    @PostExchange("/add-blacklist-list")
    void addBlacklist(@RequestBody List<String> phones);

    /**
     * 移除黑名单
     *
     * @param phone 手机号
     */
    @PostExchange("/remove-blacklist-one")
    void removeBlacklist(@RequestParam String phone);

    /**
     * 移除黑名单
     *
     * @param phones 手机号
     */
    @PostExchange("/remove-blacklist-list")
    void removeBlacklist(@RequestBody List<String> phones);

}
