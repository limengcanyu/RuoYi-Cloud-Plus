package org.dromara.job;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;

/**
 * 任务调度模块
 *
 * @author Lion Li
 */
@SpringBootApplication
public class RuoYiJobApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(RuoYiJobApplication.class);
        application.setApplicationStartup(new BufferingApplicationStartup(2048));
        application.run(args);
        System.out.println("(♥◠‿◠)ﾉﾞ  任务调度模块启动成功   ლ(´ڡ`ლ)ﾞ  ");
    }

}
