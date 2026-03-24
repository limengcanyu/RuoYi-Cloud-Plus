package org.dromara.system.api;

import org.dromara.common.core.annotation.RemoteHttpService;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.Set;

/**
 * 用户权限处理
 *
 * @author Lion Li
 */
@RemoteHttpService("ruoyi-system")
@HttpExchange("/remote/permission")
public interface RemotePermissionService {

    /**
     * 获取角色数据权限
     *
     * @param userId  用户id
     * @return 角色权限信息
     */
    @GetExchange("/role-permission")
    Set<String> getRolePermission(@RequestParam Long userId);

    /**
     * 获取菜单数据权限
     *
     * @param userId  用户id
     * @return 菜单权限信息
     */
    @GetExchange("/menu-permission")
    Set<String> getMenuPermission(@RequestParam Long userId);

}
