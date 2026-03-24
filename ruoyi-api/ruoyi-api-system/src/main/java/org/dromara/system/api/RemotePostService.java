package org.dromara.system.api;

import org.dromara.common.core.annotation.RemoteHttpService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.Collection;
import java.util.Map;

/**
 * 岗位服务
 *
 * @author Lion Li
 */
@RemoteHttpService("ruoyi-system")
@HttpExchange("/remote/post")
public interface RemotePostService {

    /**
     * 根据岗位 ID 列表查询岗位名称映射关系
     *
     * @param postIds 岗位 ID 列表
     * @return Map，其中 key 为岗位 ID，value 为对应的岗位名称
     */
    @PostExchange("/select-post-names-by-ids")
    Map<Long, String> selectPostNamesByIds(@RequestBody Collection<Long> postIds);

}
