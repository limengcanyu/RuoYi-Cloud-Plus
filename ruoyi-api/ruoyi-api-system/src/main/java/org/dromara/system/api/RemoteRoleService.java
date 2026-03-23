package org.dromara.system.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collection;
import java.util.Map;

/**
 * 角色服务
 *
 * @author Lion Li
 */
@FeignClient(contextId = "remoteRoleService", name = "ruoyi-system", path = "/remote/role", primary = false)
public interface RemoteRoleService {

    /**
     * 根据角色 ID 列表查询角色名称映射关系
     *
     * @param roleIds 角色 ID 列表
     * @return Map，其中 key 为角色 ID，value 为对应的角色名称
     */
    @PostMapping("/select-role-names-by-ids")
    Map<Long, String> selectRoleNamesByIds(@RequestBody Collection<Long> roleIds);

}

