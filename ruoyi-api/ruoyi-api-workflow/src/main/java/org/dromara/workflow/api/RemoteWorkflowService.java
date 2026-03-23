package org.dromara.workflow.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.dromara.workflow.api.domain.RemoteCompleteTask;
import org.dromara.workflow.api.domain.RemoteStartProcess;
import org.dromara.workflow.api.domain.RemoteStartProcessReturn;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 通用 工作流服务
 *
 * @Author ZETA
 * @Date 2024/6/3
 */
@FeignClient(contextId = "remoteWorkflowService", name = "ruoyi-workflow", path = "/remote/workflow",
    fallbackFactory = RemoteWorkflowServiceFallbackFactory.class, primary = false)
public interface RemoteWorkflowService {

    /**
     * 运行中的实例 删除程实例，删除历史记录，删除业务与流程关联信息
     *
     * @param businessIds 业务id
     * @return 结果
     */
    @PostMapping("/delete-instance")
    boolean deleteInstance(@RequestBody List<String> businessIds);

    /**
     * 获取当前流程状态
     *
     * @param taskId 任务id
     * @return 状态
     */
    @GetMapping("/business-status-by-task-id")
    String getBusinessStatusByTaskId(@RequestParam Long taskId);

    /**
     * 获取当前流程状态
     *
     * @param businessId 业务id
     * @return 状态
     */
    @GetMapping("/business-status")
    String getBusinessStatus(@RequestParam String businessId);

    /**
     * 设置流程变量
     *
     * @param instanceId 流程实例id
     * @param variable   流程变量
     */
    @PostMapping("/set-variable")
    void setVariable(@RequestParam Long instanceId, @RequestBody Map<String, Object> variable);

    /**
     * 获取流程变量
     *
     * @param instanceId 流程实例id
     */
    @GetMapping("/instance-variable")
    Map<String, Object> instanceVariable(@RequestParam Long instanceId);

    /**
     * 按照业务id查询流程实例id
     *
     * @param businessId 业务id
     * @return 结果
     */
    @GetMapping("/instance-id-by-business-id")
    Long getInstanceIdByBusinessId(@RequestParam String businessId);

    /**
     * 启动流程
     *
     * @param startProcess 参数
     * @return 结果
     */
    @PostMapping("/start-workflow")
    RemoteStartProcessReturn startWorkFlow(@RequestBody RemoteStartProcess startProcess);

    /**
     * 办理任务
     *
     * @param completeTask 参数
     * @return 结果
     */
    @PostMapping("/complete-task")
    boolean completeTask(@RequestBody RemoteCompleteTask completeTask);


    /**
     * 办理任务
     *
     * @param taskId  任务ID
     * @param message 办理意见
     * @return 结果
     */
    @PostMapping("/complete-task-simple")
    boolean completeTask(@RequestParam Long taskId, @RequestParam String message);

    /**
     * 启动流程并办理第一个任务
     *
     * @param startProcess 参数
     * @return 结果
     */
    @PostMapping("/start-complete-task")
    boolean startCompleteTask(@RequestBody RemoteStartProcess startProcess);

}

