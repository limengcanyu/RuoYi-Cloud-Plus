package org.dromara.system.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

/**
 * 用户权限处理
 *
 * @author Lion Li
 */
@FeignClient(contextId = "remotePermissionService", name = "ruoyi-system", path = "/remote/permission", primary = false)
public interface RemotePermissionService {

    /**
     * 获取角色数据权限
     *
     * @param userId  用户id
     * @return 角色权限信息
     */
    @GetMapping("/role-permission")
    Set<String> getRolePermission(@RequestParam Long userId);

    /**
     * 获取菜单数据权限
     *
     * @param userId  用户id
     * @return 菜单权限信息
     */
    @GetMapping("/menu-permission")
    Set<String> getMenuPermission(@RequestParam Long userId);

}

