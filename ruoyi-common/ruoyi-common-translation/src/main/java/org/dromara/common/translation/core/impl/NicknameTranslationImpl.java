package org.dromara.common.translation.core.impl;

import cn.hutool.core.convert.Convert;
import lombok.AllArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.core.constant.CacheNames;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.redis.utils.CacheUtils;
import org.dromara.common.translation.annotation.TranslationType;
import org.dromara.common.translation.constant.TransConstant;
import org.dromara.common.translation.core.TranslationInterface;
import org.dromara.system.api.RemoteUserService;

import java.util.*;

/**
 * 用户昵称翻译实现
 *
 * @author may
 */
@AllArgsConstructor
@TranslationType(type = TransConstant.USER_ID_TO_NICKNAME)
public class NicknameTranslationImpl implements TranslationInterface<String> {

    @DubboReference
    private RemoteUserService remoteUserService;

    @Override
    public String translation(Object key, String other) {
        if (key instanceof Long id) {
            String nickname = CacheUtils.get(CacheNames.SYS_NICKNAME, key);
            if (StringUtils.isNotBlank(nickname)) {
                return nickname;
            }
            return remoteUserService.selectNicknameById(id);
        } else if (key instanceof String ids) {
            List<String> list = new ArrayList<>();
            for (Long id : StringUtils.splitTo(ids, Convert::toLong)) {
                String nickname = CacheUtils.get(CacheNames.SYS_NICKNAME, id);
                if (StringUtils.isNotBlank(nickname)) {
                    list.add(nickname);
                } else {
                    list.add(remoteUserService.selectNicknameById(id));
                }
            }
            return StringUtils.joinComma(list);
        }
        return null;
    }

    @Override
    public Map<Object, String> translationBatch(Set<Object> keys, String other) {
        Set<Long> userIds = collectLongIds(keys);
        if (userIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, String> userNames = remoteUserService.selectUserNicksByIds(userIds);
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
        return source == null ? null : userNames.get(Convert.toLong(source));
    }

}
