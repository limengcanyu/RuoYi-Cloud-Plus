package org.dromara.system.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.dromara.system.api.domain.bo.RemoteSocialBo;
import org.dromara.system.api.domain.vo.RemoteSocialVo;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 社会化关系服务
 *
 * @author Michelle.Chung
 */
@FeignClient(contextId = "remoteSocialService", name = "ruoyi-system", path = "/remote/social", primary = false)
public interface RemoteSocialService {

    /**
     * 根据 authId 查询用户授权信息
     *
     * @param authId 认证id
     * @return 授权信息
     */
    @GetMapping("/select-by-auth-id")
    List<RemoteSocialVo> selectByAuthId(@RequestParam String authId);

    /**
     * 查询列表
     *
     * @param bo 社会化关系业务对象
     */
    @PostMapping("/query-list")
    List<RemoteSocialVo> queryList(@RequestBody RemoteSocialBo bo);

    /**
     * 保存社会化关系
     *
     * @param bo 社会化关系业务对象
     */
    @PostMapping("/insert-by-bo")
    void insertByBo(@RequestBody RemoteSocialBo bo);

    /**
     * 更新社会化关系
     *
     * @param bo 社会化关系业务对象
     */
    @PostMapping("/update-by-bo")
    void updateByBo(@RequestBody RemoteSocialBo bo);

    /**
     * 删除社会化关系
     *
     * @param socialId 社会化关系ID
     * @return 结果
     */
    @PostMapping("/delete-with-valid-by-id")
    Boolean deleteWithValidById(@RequestParam Long socialId);

}

