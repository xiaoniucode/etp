package com.xiaoniucode.etp.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
        SpringApplication.run(TunnelServerStartup.class, args);
    }
}
