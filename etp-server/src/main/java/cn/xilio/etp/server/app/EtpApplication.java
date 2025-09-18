package cn.xilio.etp.server.app;

import cn.xilio.etp.common.EtpBanner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("cn.xilio.etp.server")
public class EtpApplication {
    public static void main(String[] args) {
        EtpBanner.printLogo();
        SpringApplication.run(EtpApplication.class);
    }
}
