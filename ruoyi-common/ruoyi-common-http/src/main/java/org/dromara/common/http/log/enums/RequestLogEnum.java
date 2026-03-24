package org.dromara.common.http.log.enums;

import lombok.AllArgsConstructor;

/**
 * 请求日志级别.
 *
 * @author Lion Li
 */
@AllArgsConstructor
public enum RequestLogEnum {

    /**
     * 基础信息.
     */
    INFO,

    /**
     * 参数信息.
     */
    PARAM,

    /**
     * 全量信息.
     */
    FULL

}
