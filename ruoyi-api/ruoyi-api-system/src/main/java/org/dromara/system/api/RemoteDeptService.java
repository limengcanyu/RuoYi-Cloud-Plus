package org.dromara.system.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.dromara.system.api.domain.vo.RemoteDeptVo;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 部门服务
 *
 * @author Lion Li
 */
@FeignClient(contextId = "remoteDeptService", name = "ruoyi-system", path = "/remote/dept", primary = false)
public interface RemoteDeptService {

    /**
     * 通过部门ID查询部门名称
     *
     * @param deptIds 部门ID串逗号分隔
     * @return 部门名称串逗号分隔
     */
    @GetMapping("/select-dept-name-by-ids")
    String selectDeptNameByIds(@RequestParam String deptIds);

    /**
     * 根据部门ID查询部门负责人
     *
     * @param deptId 部门ID，用于指定需要查询的部门
     * @return 返回该部门的负责人ID
     */
    @GetMapping("/select-dept-leader-by-id")
    Long selectDeptLeaderById(@RequestParam Long deptId);

    /**
     * 查询部门
     *
     * @return 部门列表
     */
    @GetMapping("/select-depts-by-list")
    List<RemoteDeptVo> selectDeptsByList();

    /**
     * 根据部门 ID 列表查询部门名称映射关系
     *
     * @param deptIds 部门 ID 列表
     * @return Map，其中 key 为部门 ID，value 为对应的部门名称
     */
    @PostMapping("/select-dept-names-by-ids")
    Map<Long, String> selectDeptNamesByIds(@RequestBody Collection<Long> deptIds);

}

