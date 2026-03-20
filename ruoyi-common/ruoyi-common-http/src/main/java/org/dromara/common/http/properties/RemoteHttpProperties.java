package org.dromara.common.http.properties;

import lombok.Data;
import org.dromara.common.http.log.enums.RequestLogEnum;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 内部 HTTP 调用配置.
 *
 * @author Lion Li
 */
@Data
@ConfigurationProperties(prefix = "remote.http")
public class RemoteHttpProperties {

    /**
     * 是否开启请求日志.
     */
    private Boolean requestLog = Boolean.FALSE;

    /**
     * 日志级别.
     */
    private RequestLogEnum logLevel = RequestLogEnum.INFO;

}
