package org.dromara.resource.api;

import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.resource.api.domain.RemoteFile;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.List;

/**
 * 文件服务 fallback factory.
 *
 * @author Lion Li
 */
@Slf4j
public class RemoteFileServiceFallbackFactory implements FallbackFactory<RemoteFileService> {

    @Override
    public RemoteFileService create(Throwable cause) {
        return new RemoteFileService() {
            @Override
            public RemoteFile upload(String name, String originalFilename, String contentType, byte[] file) {
                log.warn("文件服务调用失败, 已触发 fallback", cause);
                return null;
            }

            @Override
            public String selectUrlByIds(String ossIds) {
                log.warn("文件服务调用失败, 已触发 fallback", cause);
                return StringUtils.EMPTY;
            }

            @Override
            public List<RemoteFile> selectByIds(String ossIds) {
                log.warn("文件服务调用失败, 已触发 fallback", cause);
                return List.of();
            }
        };
    }
}
