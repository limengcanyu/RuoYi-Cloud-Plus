package org.dromara.common.http.config;

import feign.RequestInterceptor;
import org.dromara.common.http.support.RemoteHttpDataPermissionCodec;
import org.dromara.common.http.support.RemoteHttpDataPermissionInterceptor;
import org.dromara.common.http.support.RemoteHttpDataPermissionRequestInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 数据权限透传自动配置。
 *
 * 仅在引入 ruoyi-common-mybatis 后生效。
 */
@AutoConfiguration
@ConditionalOnClass(name = "org.dromara.common.mybatis.helper.DataPermissionHelper")
public class RemoteHttpDataPermissionAutoConfiguration {

    @Bean
    public RemoteHttpDataPermissionCodec remoteHttpDataPermissionCodec() {
        return new RemoteHttpDataPermissionCodec();
    }

    @Bean
    public RequestInterceptor remoteHttpDataPermissionRequestInterceptor(
        RemoteHttpDataPermissionCodec remoteHttpDataPermissionCodec) {
        return new RemoteHttpDataPermissionRequestInterceptor(remoteHttpDataPermissionCodec);
    }

    @Bean
    public RemoteHttpDataPermissionInterceptor remoteHttpDataPermissionInterceptor(
        RemoteHttpDataPermissionCodec remoteHttpDataPermissionCodec) {
        return new RemoteHttpDataPermissionInterceptor(remoteHttpDataPermissionCodec);
    }

    @Bean
    public WebMvcConfigurer remoteHttpDataPermissionWebMvcConfigurer(
        RemoteHttpDataPermissionInterceptor remoteHttpDataPermissionInterceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(remoteHttpDataPermissionInterceptor);
            }
        };
    }
}
