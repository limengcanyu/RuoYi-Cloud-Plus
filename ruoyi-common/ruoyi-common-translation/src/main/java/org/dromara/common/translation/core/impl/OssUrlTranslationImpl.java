package org.dromara.common.translation.core.impl;

import cn.hutool.core.convert.Convert;
import org.dromara.common.core.utils.StreamUtils;
import org.dromara.common.translation.annotation.TranslationType;
import org.dromara.common.translation.constant.TransConstant;
import org.dromara.common.translation.core.TranslationInterface;
import org.dromara.resource.api.RemoteFileService;
import lombok.AllArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.resource.api.domain.RemoteFile;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * OSS翻译实现
 *
 * @author Lion Li
 */
@AllArgsConstructor
@TranslationType(type = TransConstant.OSS_ID_TO_URL)
public class OssUrlTranslationImpl implements TranslationInterface<String> {

    @DubboReference(mock = "true")
    private RemoteFileService remoteFileService;

    @Override
    public String translation(Object key, String other) {
        return remoteFileService.selectUrlByIds(key.toString());
    }

    @Override
    public Map<Object, String> translationBatch(Set<Object> keys, String other) {
        Set<Long> ossIds = collectLongIds(keys);
        if (ossIds.isEmpty()) {
            return Map.of();
        }
        String idText = ossIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        Map<Long, String> ossUrls = new LinkedHashMap<>(StreamUtils.toMap(remoteFileService.selectByIds(idText), RemoteFile::getOssId, RemoteFile::getUrl));
        Map<Object, String> result = new LinkedHashMap<>(keys.size());
        for (Object key : keys) {
            result.put(key, buildValue(key, ossUrls));
        }
        return result;
    }

    private String buildValue(Object source, Map<Long, String> ossUrls) {
        if (source instanceof String ids) {
            return joinMappedValues(ids, ossUrls::get);
        }
        return source == null ? null : ossUrls.get(Convert.toLong(source));
    }

}
