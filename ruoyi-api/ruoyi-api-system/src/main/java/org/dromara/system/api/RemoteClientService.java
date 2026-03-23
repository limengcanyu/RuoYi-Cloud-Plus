package org.dromara.system.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.dromara.system.api.domain.vo.RemoteClientVo;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 客户端服务
 *
 * @author Michelle.Chung
 */
@FeignClient(contextId = "remoteClientService", name = "ruoyi-system", path = "/remote/client", primary = false)
public interface RemoteClientService {

    /**
     * 根据客户端id获取客户端详情
     *
     * @param clientId 客户端id
     * @return 客户端对象
     */
    @GetMapping("/query-by-client-id")
    RemoteClientVo queryByClientId(@RequestParam String clientId);

}

