package org.dromara.common.translation.core.impl;

import cn.hutool.core.convert.Convert;
import org.dromara.common.core.constant.CacheNames;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.redis.utils.CacheUtils;
import org.dromara.common.translation.annotation.TranslationType;
import org.dromara.common.translation.constant.TransConstant;
import org.dromara.common.translation.core.TranslationInterface;
import org.dromara.system.api.RemoteUserService;
import lombok.AllArgsConstructor;

/**
 * 用户名翻译实现
 *
 * @author Lion Li
 */
@AllArgsConstructor
@TranslationType(type = TransConstant.USER_ID_TO_NAME)
public class UserNameTranslationImpl implements TranslationInterface<String> {

    private final RemoteUserService remoteUserService;

    @Override
    public String translation(Object key, String other) {
        Long userId = Convert.toLong(key);
        String username = CacheUtils.get(CacheNames.SYS_USER_NAME, userId);
        if (StringUtils.isNotBlank(username)) {
            return username;
        }
        return remoteUserService.selectUserNameById(userId);
    }
}
