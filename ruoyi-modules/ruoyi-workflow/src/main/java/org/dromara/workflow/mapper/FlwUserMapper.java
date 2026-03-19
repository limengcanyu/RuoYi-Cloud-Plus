package org.dromara.workflow.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.toolkit.JoinWrappers;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.warm.flow.orm.entity.FlowDefinition;
import org.dromara.warm.flow.orm.entity.FlowHisTask;
import org.dromara.warm.flow.orm.entity.FlowInstance;
import org.dromara.warm.flow.orm.entity.FlowUser;
import org.dromara.workflow.domain.FlowInstanceBizExt;
import org.dromara.workflow.domain.bo.FlowTaskBo;
import org.dromara.workflow.domain.vo.FlowTaskVo;

import java.util.List;

/**
 * 流程用户查询 Mapper
 */
public interface FlwUserMapper extends BaseMapperPlus<FlowUser, FlowUser> {

    default Page<FlowTaskVo> getTaskCopyByPage(Page<FlowTaskVo> page,
                                               FlowTaskBo bo,
                                               List<String> categoryIds,
                                               String userId) {
        MPJLambdaWrapper<FlowUser> wrapper = JoinWrappers.lambda("a", FlowUser.class)
            .selectAs("b", FlowHisTask::getId, FlowTaskVo::getId)
            .selectAs("b", FlowHisTask::getUpdateTime, FlowTaskVo::getUpdateTime)
            .selectAs("c", FlowInstance::getBusinessId, FlowTaskVo::getBusinessId)
            .selectAs("c", FlowInstance::getFlowStatus, FlowTaskVo::getFlowStatus)
            .selectAs("c", FlowInstance::getCreateBy, FlowTaskVo::getCreateBy)
            .selectAs(FlowUser::getProcessedBy, FlowTaskVo::getProcessedBy)
            .selectAs(FlowUser::getCreateTime, FlowTaskVo::getCreateTime)
            .selectAs("b", FlowHisTask::getFormCustom, FlowTaskVo::getFormCustom)
            .selectAs("b", FlowHisTask::getFormPath, FlowTaskVo::getFormPath)
            .selectAs("b", FlowHisTask::getNodeName, FlowTaskVo::getNodeName)
            .selectAs("b", FlowHisTask::getNodeCode, FlowTaskVo::getNodeCode)
            .selectAs("d", FlowDefinition::getFlowName, FlowTaskVo::getFlowName)
            .selectAs("d", FlowDefinition::getFlowCode, FlowTaskVo::getFlowCode)
            .selectAs("d", FlowDefinition::getCategory, FlowTaskVo::getCategory)
            .selectAs("d", FlowDefinition::getVersion, FlowTaskVo::getVersion)
            .selectAs("biz", FlowInstanceBizExt::getBusinessCode, FlowTaskVo::getBusinessCode)
            .selectAs("biz", FlowInstanceBizExt::getBusinessTitle, FlowTaskVo::getBusinessTitle)
            .leftJoin(FlowHisTask.class, "b", FlowHisTask::getTaskId, FlowUser::getAssociated)
            .leftJoin(FlowInstance.class, "c", FlowInstance::getId, FlowHisTask::getInstanceId)
            .leftJoin(FlowDefinition.class, "d", FlowDefinition::getId, FlowInstance::getDefinitionId)
            .leftJoin(FlowInstanceBizExt.class, "biz", FlowInstanceBizExt::getInstanceId, FlowInstance::getId)
            .eq("a", FlowUser::getType, "4")
            .eq("a", FlowUser::getDelFlag, "0")
            .eq("b", FlowHisTask::getDelFlag, "0")
            .eq("d", FlowDefinition::getDelFlag, "0")
            .like(hasText(bo.getNodeName()), "b", FlowHisTask::getNodeName, bo.getNodeName())
            .like(hasText(bo.getFlowName()), "d", FlowDefinition::getFlowName, bo.getFlowName())
            .like(hasText(bo.getFlowCode()), "d", FlowDefinition::getFlowCode, bo.getFlowCode())
            .like(hasText(bo.getFlowStatus()), "c", FlowInstance::getFlowStatus, bo.getFlowStatus())
            .in(hasItems(bo.getCreateByIds()), "c", FlowInstance::getCreateBy, bo.getCreateByIds())
            .in(hasItems(categoryIds), "d", FlowDefinition::getCategory, categoryIds)
            .between(hasBetween(bo), "a", FlowUser::getCreateTime, bo.getParams().get("beginTime"), bo.getParams().get("endTime"))
            .eq(StringUtils.isNotBlank(userId), "a", FlowUser::getProcessedBy, userId)
            .orderByDesc("a", FlowUser::getCreateTime)
            .orderByDesc("b", FlowHisTask::getUpdateTime);
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
