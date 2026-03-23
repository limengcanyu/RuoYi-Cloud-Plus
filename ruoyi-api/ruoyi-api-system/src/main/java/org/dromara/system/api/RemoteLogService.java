package org.dromara.system.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.dromara.system.api.domain.bo.RemoteLoginInfoBo;
import org.dromara.system.api.domain.bo.RemoteOperLogBo;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 日志服务
 *
 * @author Lion Li
 */
@FeignClient(contextId = "remoteLogService", name = "ruoyi-system", path = "/remote/log", primary = false)
public interface RemoteLogService {

    /**
     * 保存系统日志
     *
     * @param sysOperLog 日志实体
     */
    @PostMapping("/save-log")
    void saveLog(@RequestBody RemoteOperLogBo sysOperLog);

    /**
     * 保存访问记录
     *
     * @param sysLoginInfo 访问实体
     */
    @PostMapping("/save-login-info")
    void saveLoginInfo(@RequestBody RemoteLoginInfoBo sysLoginInfo);

}

