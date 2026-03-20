package org.dromara.system.dubbo;

import lombok.RequiredArgsConstructor;
import org.dromara.common.http.annotation.RemoteServiceController;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.system.api.RemoteClientService;
import org.dromara.system.api.domain.vo.RemoteClientVo;
import org.dromara.system.domain.vo.SysClientVo;
import org.dromara.system.service.ISysClientService;

/**
 * 客户端服务
 *
 * @author Michelle.Chung
 */
@RequiredArgsConstructor
@RemoteServiceController
public class RemoteClientServiceImpl implements RemoteClientService {

    private final ISysClientService sysClientService;

    /**
     * 根据客户端id获取客户端详情
     *
     * @see org.dromara.system.domain.convert.SysClientVoConvert
     */
    @Override
    public RemoteClientVo queryByClientId(String clientId) {
        SysClientVo vo = sysClientService.queryByClientId(clientId);
        return MapstructUtils.convert(vo, RemoteClientVo.class);
    }

}
