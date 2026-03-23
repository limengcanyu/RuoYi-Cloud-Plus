package org.dromara.resource.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.dromara.common.core.exception.ServiceException;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 邮件服务
 *
 * @author Lion Li
 */
@FeignClient(contextId = "remoteMailService", name = "ruoyi-resource", path = "/remote/mail", primary = false)
public interface RemoteMailService {

    /**
     * 发送邮件
     *
     * @param to      接收人
     * @param subject 标题
     * @param text    内容
     */
    @PostMapping("/send")
    void send(@RequestParam String to, @RequestParam String subject, @RequestParam String text) throws ServiceException;

}


