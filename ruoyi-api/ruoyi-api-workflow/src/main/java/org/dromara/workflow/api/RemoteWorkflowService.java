package org.dromara.workflow.api;

import org.dromara.common.core.annotation.RemoteHttpService;
import org.dromara.workflow.api.domain.RemoteCompleteTask;
import org.dromara.workflow.api.domain.RemoteStartProcess;
import org.dromara.workflow.api.domain.RemoteStartProcessReturn;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;
import java.util.Map;

/**
 * 通用 工作流服务
 *
 * @Author ZETA
 * @Date 2024/6/3
 */
@RemoteHttpService(value = "ruoyi-workflow", fallback = RemoteWorkflowServiceFallback.class)
@HttpExchange("/remote/workflow")
public interface RemoteWorkflowService {

    /**
     * 运行中的实例 删除程实例，删除历史记录，删除业务与流程关联信息
     *
     * @param businessIds 业务id
     * @return 结果
     */
    @PostExchange("/delete-instance")
    boolean deleteInstance(@RequestBody List<String> businessIds);

    /**
     * 获取当前流程状态
     *
     * @param taskId 任务id
     * @return 状态
     */
    @GetExchange("/business-status-by-task-id")
    String getBusinessStatusByTaskId(@RequestParam Long taskId);

    /**
     * 获取当前流程状态
     *
     * @param businessId 业务id
     * @return 状态
     */
    @GetExchange("/business-status")
    String getBusinessStatus(@RequestParam String businessId);

    /**
     * 设置流程变量
     *
     * @param instanceId 流程实例id
     * @param variable   流程变量
     */
    @PostExchange("/set-variable")
    void setVariable(@RequestParam Long instanceId, @RequestBody Map<String, Object> variable);

    /**
     * 获取流程变量
     *
     * @param instanceId 流程实例id
     */
    @GetExchange("/instance-variable")
    Map<String, Object> instanceVariable(@RequestParam Long instanceId);

    /**
     * 按照业务id查询流程实例id
     *
     * @param businessId 业务id
     * @return 结果
     */
    @GetExchange("/instance-id-by-business-id")
    Long getInstanceIdByBusinessId(@RequestParam String businessId);

    /**
     * 启动流程
     *
     * @param startProcess 参数
     * @return 结果
     */
    @PostExchange("/start-workflow")
    RemoteStartProcessReturn startWorkFlow(@RequestBody RemoteStartProcess startProcess);

    /**
     * 办理任务
     *
     * @param completeTask 参数
     * @return 结果
     */
    @PostExchange("/complete-task")
    boolean completeTask(@RequestBody RemoteCompleteTask completeTask);


    /**
     * 办理任务
     *
     * @param taskId  任务ID
     * @param message 办理意见
     * @return 结果
     */
    @PostExchange("/complete-task-simple")
    boolean completeTask(@RequestParam Long taskId, @RequestParam String message);

    /**
     * 启动流程并办理第一个任务
     *
     * @param startProcess 参数
     * @return 结果
     */
    @PostExchange("/start-complete-task")
    boolean startCompleteTask(@RequestBody RemoteStartProcess startProcess);

}
