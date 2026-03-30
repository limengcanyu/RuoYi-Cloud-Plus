package org.dromara.common.translation.core.impl;

import cn.hutool.core.convert.Convert;
import org.dromara.common.core.constant.CacheNames;
import org.dromara.common.core.utils.StreamUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.redis.utils.CacheUtils;
import org.dromara.common.translation.annotation.TranslationType;
import org.dromara.common.translation.constant.TransConstant;
import org.dromara.common.translation.core.TranslationInterface;
import org.dromara.system.api.RemoteUserService;
import lombok.AllArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.system.api.domain.vo.RemoteUserVo;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 用户名翻译实现
 *
 * @author Lion Li
 */
@AllArgsConstructor
@TranslationType(type = TransConstant.USER_ID_TO_NAME)
public class UserNameTranslationImpl implements TranslationInterface<String> {

    @DubboReference
    private RemoteUserService remoteUserService;

    @Override
    public String translation(Object key, String other) {
        Long userId = Convert.toLong(key);
        String username = CacheUtils.get(CacheNames.SYS_USER_NAME, userId);
        if (StringUtils.isNotBlank(username)) {
            return username;
        }
        return remoteUserService.selectUserNameById(userId);
    }

    @Override
    public Map<Object, String> translationBatch(Set<Object> keys, String other) {
        Set<Long> userIds = collectLongIds(keys);
        if (userIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, String> userNames = new LinkedHashMap<>(StreamUtils.toMap(remoteUserService.selectListByIds(userIds), RemoteUserVo::getUserId, RemoteUserVo::getUserName));
        Map<Object, String> result = new LinkedHashMap<>(keys.size());
        for (Object key : keys) {
            result.put(key, buildValue(key, userNames));
        }
        return result;
    }

    private String buildValue(Object source, Map<Long, String> userNames) {
        if (source instanceof String ids) {
            return joinMappedValues(ids, userNames::get);
        }
        return userNames.get(Convert.toLong(source));
    }

}
