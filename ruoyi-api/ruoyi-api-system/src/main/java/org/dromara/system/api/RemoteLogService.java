package org.dromara.system.api;

import org.dromara.common.core.annotation.RemoteHttpService;
import org.dromara.system.api.domain.bo.RemoteLoginInfoBo;
import org.dromara.system.api.domain.bo.RemoteOperLogBo;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * 日志服务
 *
 * @author Lion Li
 */
@RemoteHttpService("ruoyi-system")
@HttpExchange("/remote/log")
public interface RemoteLogService {

    /**
     * 保存系统日志
     *
     * @param sysOperLog 日志实体
     */
    @PostExchange("/save-log")
    void saveLog(@RequestBody RemoteOperLogBo sysOperLog);

    /**
     * 保存访问记录
     *
     * @param sysLoginInfo 访问实体
     */
    @PostExchange("/save-login-info")
    void saveLoginInfo(@RequestBody RemoteLoginInfoBo sysLoginInfo);

}
