package org.dromara.resource.service;

import org.dromara.common.push.dto.PushPayloadDTO;
import org.dromara.resource.domain.vo.SysMessageBoxVo;

import java.util.List;

/**
 * 消息记录服务接口
 *
 * @author Lion Li
 */
public interface ISysMessageService {

    SysMessageBoxVo queryMessageBox(Long userId);

    PushPayloadDTO storeAll(PushPayloadDTO payload);

    PushPayloadDTO storeUsers(List<Long> userIds, PushPayloadDTO payload);
}
