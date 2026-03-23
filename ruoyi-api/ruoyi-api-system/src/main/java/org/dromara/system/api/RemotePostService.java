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
 * 岗位服务
 *
 * @author Lion Li
 */
@FeignClient(contextId = "remotePostService", name = "ruoyi-system", path = "/remote/post", primary = false)
public interface RemotePostService {

    /**
     * 根据岗位 ID 列表查询岗位名称映射关系
     *
     * @param postIds 岗位 ID 列表
     * @return Map，其中 key 为岗位 ID，value 为对应的岗位名称
     */
    @PostMapping("/select-post-names-by-ids")
    Map<Long, String> selectPostNamesByIds(@RequestBody Collection<Long> postIds);

}

