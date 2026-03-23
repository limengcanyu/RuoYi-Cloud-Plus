package org.dromara.common.http.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 内部 HTTP 服务控制器.
 *
 * @author Lion Li
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RestController
@RequestMapping
@Primary
public @interface RemoteServiceController {

    @AliasFor(annotation = RestController.class, attribute = "value")
    String value() default "";

    @AliasFor(annotation = RequestMapping.class, attribute = "path")
    String[] path() default {};
}
