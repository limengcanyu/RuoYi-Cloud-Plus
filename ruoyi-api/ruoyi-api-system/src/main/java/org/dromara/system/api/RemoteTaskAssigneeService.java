package org.dromara.system.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.dromara.system.api.domain.bo.RemoteTaskAssigneeBo;
import org.dromara.system.api.domain.vo.RemoteTaskAssigneeVo;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 工作流设计器获取任务执行人
 *
 * @author Lion Li
 */
@FeignClient(contextId = "remoteTaskAssigneeService", name = "ruoyi-system", path = "/remote/task-assignee", primary = false)
public interface RemoteTaskAssigneeService {

    /**
     * 查询角色并返回任务指派的列表，支持分页
     *
     * @param taskQuery 查询条件
     * @return 办理人
     */
    @PostMapping("/select-roles")
    RemoteTaskAssigneeVo selectRolesByTaskAssigneeList(@RequestBody RemoteTaskAssigneeBo taskQuery);

    /**
     * 查询岗位并返回任务指派的列表，支持分页
     *
     * @param taskQuery 查询条件
     * @return 办理人
     */
    @PostMapping("/select-posts")
    RemoteTaskAssigneeVo selectPostsByTaskAssigneeList(@RequestBody RemoteTaskAssigneeBo taskQuery);

    /**
     * 查询部门并返回任务指派的列表，支持分页
     *
     * @param taskQuery 查询条件
     * @return 办理人
     */
    @PostMapping("/select-depts")
    RemoteTaskAssigneeVo selectDeptsByTaskAssigneeList(@RequestBody RemoteTaskAssigneeBo taskQuery);

    /**
     * 查询用户并返回任务指派的列表，支持分页
     *
     * @param taskQuery 查询条件
     * @return 办理人
     */
    @PostMapping("/select-users")
    RemoteTaskAssigneeVo selectUsersByTaskAssigneeList(@RequestBody RemoteTaskAssigneeBo taskQuery);

}

