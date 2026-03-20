package org.dromara.system.api;

import org.dromara.common.core.annotation.RemoteHttpService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.Collection;
import java.util.Map;

/**
 * 角色服务
 *
 * @author Lion Li
 */
@RemoteHttpService("ruoyi-system")
@HttpExchange("/remote/role")
public interface RemoteRoleService {

    /**
     * 根据角色 ID 列表查询角色名称映射关系
     *
     * @param roleIds 角色 ID 列表
     * @return Map，其中 key 为角色 ID，value 为对应的角色名称
     */
    @PostExchange("/select-role-names-by-ids")
    Map<Long, String> selectRoleNamesByIds(@RequestBody Collection<Long> roleIds);

}
