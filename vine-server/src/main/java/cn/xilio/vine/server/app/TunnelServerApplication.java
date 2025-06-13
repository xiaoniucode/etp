package cn.xilio.vine.server.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("cn.xilio.vine.server")
public class TunnelServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(TunnelServerApplication.class);
    }
}
