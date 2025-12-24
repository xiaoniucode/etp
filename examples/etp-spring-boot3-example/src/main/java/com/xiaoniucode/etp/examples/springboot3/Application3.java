package com.xiaoniucode.etp.examples.springboot3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author liuxin
 */
@SpringBootApplication(scanBasePackages = "com.xiaoniucode.etp")
public class Application3 {

    public static void main(String[] args) {
        SpringApplication.run(Application3.class, args);
    }

}
