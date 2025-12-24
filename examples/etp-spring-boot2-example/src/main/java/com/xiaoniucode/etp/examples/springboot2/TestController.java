package com.xiaoniucode.etp.examples.springboot2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author liuxin
 */
@RestController
@RequestMapping("/")
public class TestController {
    @Value("${spring.application.name}")
    private String appName;
    @GetMapping
    public String sayHello() {
        return "Hello " + appName;
    }
}
