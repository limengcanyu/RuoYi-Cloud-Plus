package org.dromara.system.api;

import org.dromara.common.core.annotation.RemoteHttpService;
import org.dromara.system.api.domain.bo.RemoteTaskAssigneeBo;
import org.dromara.system.api.domain.vo.RemoteTaskAssigneeVo;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * 工作流设计器获取任务执行人
 *
 * @author Lion Li
 */
@RemoteHttpService("ruoyi-system")
@HttpExchange("/remote/task-assignee")
public interface RemoteTaskAssigneeService {

    /**
     * 查询角色并返回任务指派的列表，支持分页
     *
     * @param taskQuery 查询条件
     * @return 办理人
     */
    @PostExchange("/select-roles")
    RemoteTaskAssigneeVo selectRolesByTaskAssigneeList(@RequestBody RemoteTaskAssigneeBo taskQuery);

    /**
     * 查询岗位并返回任务指派的列表，支持分页
     *
     * @param taskQuery 查询条件
     * @return 办理人
     */
    @PostExchange("/select-posts")
    RemoteTaskAssigneeVo selectPostsByTaskAssigneeList(@RequestBody RemoteTaskAssigneeBo taskQuery);

    /**
     * 查询部门并返回任务指派的列表，支持分页
     *
     * @param taskQuery 查询条件
     * @return 办理人
     */
    @PostExchange("/select-depts")
    RemoteTaskAssigneeVo selectDeptsByTaskAssigneeList(@RequestBody RemoteTaskAssigneeBo taskQuery);

    /**
     * 查询用户并返回任务指派的列表，支持分页
     *
     * @param taskQuery 查询条件
     * @return 办理人
     */
    @PostExchange("/select-users")
    RemoteTaskAssigneeVo selectUsersByTaskAssigneeList(@RequestBody RemoteTaskAssigneeBo taskQuery);

}
