package org.dromara.resource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

/**
 * 资源服务
 *
 * @author Lion Li
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class RuoYiResourceApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(RuoYiResourceApplication.class);
        application.setApplicationStartup(new BufferingApplicationStartup(2048));
        application.run(args);
        System.out.println("(♥◠‿◠)ﾉﾞ  资源服务模块启动成功   ლ(´ڡ`ლ)ﾞ  ");
    }
}
