package org.dromara.system.api;

import org.dromara.common.core.annotation.RemoteHttpService;
import org.dromara.system.api.domain.vo.RemoteClientVo;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

/**
 * 客户端服务
 *
 * @author Michelle.Chung
 */
@RemoteHttpService("ruoyi-system")
@HttpExchange("/remote/client")
public interface RemoteClientService {

    /**
     * 根据客户端id获取客户端详情
     *
     * @param clientId 客户端id
     * @return 客户端对象
     */
    @GetExchange("/query-by-client-id")
    RemoteClientVo queryByClientId(@RequestParam String clientId);

}
