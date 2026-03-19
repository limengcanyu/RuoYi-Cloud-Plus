package org.dromara.workflow.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.toolkit.JoinWrappers;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.dromara.common.core.enums.BusinessStatusEnum;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.warm.flow.core.enums.NodeType;
import org.dromara.warm.flow.orm.entity.FlowDefinition;
import org.dromara.warm.flow.orm.entity.FlowInstance;
import org.dromara.warm.flow.orm.entity.FlowTask;
import org.dromara.warm.flow.orm.entity.FlowUser;
import org.dromara.workflow.domain.FlowInstanceBizExt;
import org.dromara.workflow.domain.bo.FlowTaskBo;
import org.dromara.workflow.domain.vo.FlowTaskVo;

import java.util.List;

/**
 * 任务信息Mapper接口
 *
 * @author may
 * @date 2024-03-02
 */
public interface FlwTaskMapper extends BaseMapperPlus<FlowTask, FlowTaskVo> {

    default Page<FlowTaskVo> getListRunTask(Page<FlowTaskVo> page,
                                            FlowTaskBo bo,
                                            List<String> categoryIds,
                                            String userId) {
        MPJLambdaWrapper<FlowTask> wrapper = JoinWrappers.lambda("t", FlowTask.class)
            .distinct()
            .selectAs(FlowTask::getId, FlowTaskVo::getId)
            .selectAs(FlowTask::getNodeCode, FlowTaskVo::getNodeCode)
            .selectAs(FlowTask::getNodeName, FlowTaskVo::getNodeName)
            .selectAs(FlowTask::getNodeType, FlowTaskVo::getNodeType)
            .selectAs(FlowTask::getDefinitionId, FlowTaskVo::getDefinitionId)
            .selectAs(FlowTask::getInstanceId, FlowTaskVo::getInstanceId)
            .selectAs(FlowTask::getCreateTime, FlowTaskVo::getCreateTime)
            .selectAs(FlowTask::getUpdateTime, FlowTaskVo::getUpdateTime)
            .selectAs("i", FlowInstance::getBusinessId, FlowTaskVo::getBusinessId)
            .selectAs("i", FlowInstance::getFlowStatus, FlowTaskVo::getFlowStatus)
            .selectAs("i", FlowInstance::getCreateBy, FlowTaskVo::getCreateBy)
            .selectAs("d", FlowDefinition::getFlowName, FlowTaskVo::getFlowName)
            .selectAs("d", FlowDefinition::getFlowCode, FlowTaskVo::getFlowCode)
            .selectAs("d", FlowDefinition::getFormCustom, FlowTaskVo::getFormCustom)
            .selectAs("d", FlowDefinition::getCategory, FlowTaskVo::getCategory)
            .selectAs("d", FlowDefinition::getVersion, FlowTaskVo::getVersion)
            .selectAs("uu", FlowUser::getProcessedBy, FlowTaskVo::getProcessedBy)
            .selectAs("uu", FlowUser::getType, FlowTaskVo::getType)
            .selectAs("biz", FlowInstanceBizExt::getBusinessCode, FlowTaskVo::getBusinessCode)
            .selectAs("biz", FlowInstanceBizExt::getBusinessTitle, FlowTaskVo::getBusinessTitle)
            .selectAs("COALESCE(NULLIF(TRIM(t.form_path), ''), NULLIF(TRIM(d.form_path), ''))", FlowTaskVo::getFormPath)
            .leftJoin(FlowUser.class, "uu", FlowUser::getAssociated, FlowTask::getId)
            .leftJoin(FlowDefinition.class, "d", FlowDefinition::getId, FlowTask::getDefinitionId)
            .leftJoin(FlowInstance.class, "i", FlowInstance::getId, FlowTask::getInstanceId)
            .leftJoin(FlowInstanceBizExt.class, "biz", FlowInstanceBizExt::getInstanceId, FlowInstance::getId)
            .eq("t", FlowTask::getNodeType, NodeType.BETWEEN.getKey())
            .eq("t", FlowTask::getDelFlag, "0")
            .eq("uu", FlowUser::getDelFlag, "0")
            .in("uu", FlowUser::getType, List.of("1", "2", "3"))
            .like(hasText(bo.getNodeName()), "t", FlowTask::getNodeName, bo.getNodeName())
            .like(hasText(bo.getFlowName()), "d", FlowDefinition::getFlowName, bo.getFlowName())
            .like(hasText(bo.getFlowCode()), "d", FlowDefinition::getFlowCode, bo.getFlowCode())
            .like(hasText(bo.getFlowStatus()), "i", FlowInstance::getFlowStatus, bo.getFlowStatus())
            .in(hasItems(bo.getCreateByIds()), "i", FlowInstance::getCreateBy, bo.getCreateByIds())
            .in(hasItems(categoryIds), "d", FlowDefinition::getCategory, categoryIds)
            .between(hasBetween(bo), "t", FlowTask::getCreateTime, bo.getParams().get("beginTime"), bo.getParams().get("endTime"))
            .eq(StringUtils.isNotBlank(userId), "uu", FlowUser::getProcessedBy, userId)
            .eq(StringUtils.isNotBlank(userId), "i", FlowInstance::getFlowStatus, BusinessStatusEnum.WAITING.getStatus())
            .orderByDesc("t", FlowTask::getCreateTime)
            .orderByDesc("t", FlowTask::getUpdateTime);
        return wrapper.page(page, FlowTaskVo.class);
    }

    default boolean hasText(String value) {
        return StringUtils.isNotBlank(value);
    }

    default boolean hasItems(List<?> values) {
        return values != null && !values.isEmpty();
    }

    default boolean hasBetween(FlowTaskBo bo) {
        return bo != null && bo.getParams() != null && bo.getParams().get("beginTime") != null && bo.getParams().get("endTime") != null;
    }

}
