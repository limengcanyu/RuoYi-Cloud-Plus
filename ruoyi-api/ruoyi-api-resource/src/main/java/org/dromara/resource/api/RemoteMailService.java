package org.dromara.resource.api;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.annotation.RemoteHttpService;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * 邮件服务
 *
 * @author Lion Li
 */
@RemoteHttpService("ruoyi-resource")
@HttpExchange("/remote/mail")
public interface RemoteMailService {

    /**
     * 发送邮件
     *
     * @param to      接收人
     * @param subject 标题
     * @param text    内容
     */
    @PostExchange("/send")
    void send(@RequestParam String to, @RequestParam String subject, @RequestParam String text) throws ServiceException;

}
