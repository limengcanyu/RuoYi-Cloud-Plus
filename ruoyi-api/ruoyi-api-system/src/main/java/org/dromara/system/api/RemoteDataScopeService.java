package org.dromara.system.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 数据权限服务
 *
 * @author Lion Li
 */
@FeignClient(contextId = "remoteDataScopeService", name = "ruoyi-system", path = "/remote/data-scope", primary = false)
public interface RemoteDataScopeService {

    /**
     * 获取角色自定义权限语句
     *
     * @param roleId 角色ID
     * @return 返回角色的自定义权限语句，如果没有找到则返回 null
     */
    @GetMapping("/role-custom")
    String getRoleCustom(@RequestParam Long roleId);

    /**
     * 获取部门和下级权限语句
     *
     * @param deptId 部门ID
     * @return 返回部门及其下级的权限语句，如果没有找到则返回 null
     */
    @GetMapping("/dept-and-child")
    String getDeptAndChild(@RequestParam Long deptId);

}

