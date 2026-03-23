package org.dromara.workflow.api;

import lombok.extern.slf4j.Slf4j;
import org.dromara.workflow.api.domain.RemoteCompleteTask;
import org.dromara.workflow.api.domain.RemoteStartProcess;
import org.dromara.workflow.api.domain.RemoteStartProcessReturn;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.List;
import java.util.Map;

/**
 * 工作流服务 fallback factory.
 *
 * @author Lion Li
 */
@Slf4j
public class RemoteWorkflowServiceFallbackFactory implements FallbackFactory<RemoteWorkflowService> {

    @Override
    public RemoteWorkflowService create(Throwable cause) {
        return new RemoteWorkflowService() {
            @Override
            public boolean deleteInstance(List<String> businessIds) {
                log.warn("工作流服务调用失败, 已触发 fallback", cause);
                return false;
            }

            @Override
            public String getBusinessStatusByTaskId(Long taskId) {
                log.warn("工作流服务调用失败, 已触发 fallback", cause);
                return null;
            }

            @Override
            public String getBusinessStatus(String businessId) {
                log.warn("工作流服务调用失败, 已触发 fallback", cause);
                return null;
            }

            @Override
            public void setVariable(Long instanceId, Map<String, Object> variable) {
                log.warn("工作流服务调用失败, 已触发 fallback", cause);
            }

            @Override
            public Map<String, Object> instanceVariable(Long instanceId) {
                log.warn("工作流服务调用失败, 已触发 fallback", cause);
                return null;
            }

            @Override
            public Long getInstanceIdByBusinessId(String businessId) {
                log.warn("工作流服务调用失败, 已触发 fallback", cause);
                return null;
            }

            @Override
            public RemoteStartProcessReturn startWorkFlow(RemoteStartProcess startProcess) {
                log.warn("工作流服务调用失败, 已触发 fallback", cause);
                return null;
            }

            @Override
            public boolean completeTask(RemoteCompleteTask completeTask) {
                log.warn("工作流服务调用失败, 已触发 fallback", cause);
                return false;
            }

            @Override
            public boolean completeTask(Long taskId, String message) {
                log.warn("工作流服务调用失败, 已触发 fallback", cause);
                return false;
            }

            @Override
            public boolean startCompleteTask(RemoteStartProcess startProcess) {
                log.warn("工作流服务调用失败, 已触发 fallback", cause);
                return false;
            }
        };
    }
}
