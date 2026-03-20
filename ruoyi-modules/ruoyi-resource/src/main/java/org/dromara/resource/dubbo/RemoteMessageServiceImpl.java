package org.dromara.resource.dubbo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.http.annotation.RemoteServiceController;
import org.dromara.common.sse.dto.SseMessageDTO;
import org.dromara.common.sse.utils.SseMessageUtils;
import org.dromara.resource.api.RemoteMessageService;

import java.util.List;

/**
 * 消息服务
 *
 * @author Lion Li
 */
@Slf4j
@RequiredArgsConstructor
@RemoteServiceController
public class RemoteMessageServiceImpl implements RemoteMessageService {

    /**
     * 发送消息
     *
     * @param sessionKey session主键 一般为用户id
     * @param message    消息文本
     */
    @Override
    public void publishMessage(List<Long> sessionKey, String message) {
        SseMessageDTO dto = new SseMessageDTO();
        dto.setMessage(message);
        dto.setUserIds(sessionKey);
        SseMessageUtils.publishMessage(dto);
    }

    /**
     * 发布订阅的消息(群发)
     *
     * @param message 消息内容
     */
    @Override
    public void publishAll(String message) {
        SseMessageUtils.publishAll(message);
    }

}
