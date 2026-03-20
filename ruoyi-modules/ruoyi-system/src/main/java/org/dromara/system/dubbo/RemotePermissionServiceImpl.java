package org.dromara.system.dubbo;

import lombok.RequiredArgsConstructor;
import org.dromara.common.http.annotation.RemoteServiceController;
import org.dromara.system.api.RemotePermissionService;
import org.dromara.system.service.ISysPermissionService;

import java.util.Set;

/**
 * 权限服务
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@RemoteServiceController
public class RemotePermissionServiceImpl implements RemotePermissionService {

    private final ISysPermissionService permissionService;

    @Override
    public Set<String> getRolePermission(Long userId) {
        return permissionService.getRolePermission(userId);
    }

    @Override
    public Set<String> getMenuPermission(Long userId) {
        return permissionService.getMenuPermission(userId);
    }
}
