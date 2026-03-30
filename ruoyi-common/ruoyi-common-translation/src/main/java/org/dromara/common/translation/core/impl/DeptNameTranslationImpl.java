package org.dromara.common.translation.core.impl;

import cn.hutool.core.convert.Convert;
import org.dromara.common.translation.annotation.TranslationType;
import org.dromara.common.translation.constant.TransConstant;
import org.dromara.common.translation.core.TranslationInterface;
import org.dromara.system.api.RemoteDeptService;
import lombok.AllArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 部门翻译实现
 *
 * @author Lion Li
 */
@AllArgsConstructor
@TranslationType(type = TransConstant.DEPT_ID_TO_NAME)
public class DeptNameTranslationImpl implements TranslationInterface<String> {

    @DubboReference
    private RemoteDeptService remoteDeptService;

    @Override
    public String translation(Object key, String other) {
        return remoteDeptService.selectDeptNameByIds(key.toString());
    }

    @Override
    public Map<Object, String> translationBatch(Set<Object> keys, String other) {
        Set<Long> deptIds = collectLongIds(keys);
        if (deptIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, String> deptNames = remoteDeptService.selectDeptNamesByIds(deptIds);
        Map<Object, String> result = new LinkedHashMap<>(keys.size());
        for (Object key : keys) {
            result.put(key, buildValue(key, deptNames));
        }
        return result;
    }

    private String buildValue(Object source, Map<Long, String> deptNames) {
        if (source instanceof String ids) {
            return joinMappedValues(ids, deptNames::get);
        }
        return source == null ? null : deptNames.get(Convert.toLong(source));
    }

}
