package org.dromara.system.api;

import org.dromara.common.core.annotation.RemoteHttpService;
import org.dromara.system.api.domain.bo.RemoteSocialBo;
import org.dromara.system.api.domain.vo.RemoteSocialVo;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

/**
 * 社会化关系服务
 *
 * @author Michelle.Chung
 */
@RemoteHttpService("ruoyi-system")
@HttpExchange("/remote/social")
public interface RemoteSocialService {

    /**
     * 根据 authId 查询用户授权信息
     *
     * @param authId 认证id
     * @return 授权信息
     */
    @GetExchange("/select-by-auth-id")
    List<RemoteSocialVo> selectByAuthId(@RequestParam String authId);

    /**
     * 查询列表
     *
     * @param bo 社会化关系业务对象
     */
    @PostExchange("/query-list")
    List<RemoteSocialVo> queryList(@RequestBody RemoteSocialBo bo);

    /**
     * 保存社会化关系
     *
     * @param bo 社会化关系业务对象
     */
    @PostExchange("/insert-by-bo")
    void insertByBo(@RequestBody RemoteSocialBo bo);

    /**
     * 更新社会化关系
     *
     * @param bo 社会化关系业务对象
     */
    @PostExchange("/update-by-bo")
    void updateByBo(@RequestBody RemoteSocialBo bo);

    /**
     * 删除社会化关系
     *
     * @param socialId 社会化关系ID
     * @return 结果
     */
    @PostExchange("/delete-with-valid-by-id")
    Boolean deleteWithValidById(@RequestParam Long socialId);

}
