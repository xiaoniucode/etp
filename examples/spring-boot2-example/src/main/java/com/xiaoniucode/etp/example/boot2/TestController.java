package com.xiaoniucode.etp.example.boot2;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author liuxin
 */
@RestController
@RequestMapping()
public class TestController {
    @GetMapping()
    public String sayHello(){
        return "hello world";
    }
}
