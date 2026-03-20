package org.dromara.common.core.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明远程 HTTP Service 所属服务.
 *
 * @author Lion Li
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RemoteHttpService {

    /**
     * 服务名.
     */
    @AliasFor("serviceId")
    String value() default "";

    /**
     * 服务名.
     */
    @AliasFor("value")
    String serviceId() default "";

    /**
     * 远程调用失败时的 fallback 实现.
     */
    Class<?> fallback() default void.class;

}
