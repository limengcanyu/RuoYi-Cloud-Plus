package org.dromara.system.api;

import org.dromara.common.core.annotation.RemoteHttpService;
import org.dromara.system.api.domain.vo.RemoteDeptVo;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 部门服务
 *
 * @author Lion Li
 */
@RemoteHttpService("ruoyi-system")
@HttpExchange("/remote/dept")
public interface RemoteDeptService {

    /**
     * 通过部门ID查询部门名称
     *
     * @param deptIds 部门ID串逗号分隔
     * @return 部门名称串逗号分隔
     */
    @GetExchange("/select-dept-name-by-ids")
    String selectDeptNameByIds(@RequestParam String deptIds);

    /**
     * 根据部门ID查询部门负责人
     *
     * @param deptId 部门ID，用于指定需要查询的部门
     * @return 返回该部门的负责人ID
     */
    @GetExchange("/select-dept-leader-by-id")
    Long selectDeptLeaderById(@RequestParam Long deptId);

    /**
     * 查询部门
     *
     * @return 部门列表
     */
    @GetExchange("/select-depts-by-list")
    List<RemoteDeptVo> selectDeptsByList();

    /**
     * 根据部门 ID 列表查询部门名称映射关系
     *
     * @param deptIds 部门 ID 列表
     * @return Map，其中 key 为部门 ID，value 为对应的部门名称
     */
    @PostExchange("/select-dept-names-by-ids")
    Map<Long, String> selectDeptNamesByIds(@RequestBody Collection<Long> deptIds);

}
