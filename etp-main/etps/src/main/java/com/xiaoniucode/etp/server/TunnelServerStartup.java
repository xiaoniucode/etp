package com.xiaoniucode.etp.server;

import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.config.domain.Dashboard;
import com.xiaoniucode.etp.server.config.ConfigParser;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 服务启动入口
 *
 * @author liuxin
 */
@EnableScheduling
@SpringBootApplication
public class TunnelServerStartup {
    public static void main(String[] args) {
        AppConfig config = ConfigParser.parse(args);
        Dashboard dashboard = config.getDashboard();
        SpringApplicationBuilder builder = new SpringApplicationBuilder(TunnelServerStartup.class);
        if (dashboard.getEnable()) {
            builder.properties("spring.main.web-application-type=servlet", "server.port=" + dashboard.getPort());
        } else {
            builder.properties("spring.main.web-application-type=none");
        }
        builder.sources(TunnelServerStartup.class)
                .initializers(context ->
                        context.getBeanFactory().registerSingleton("appConfig", config))
                .run(args);
    }
}
