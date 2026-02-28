package com.xiaoniucode.etptest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class TestController {
    @GetMapping
    public String test() {
        System.out.println("success!");
        return "say hello";
    }
}
