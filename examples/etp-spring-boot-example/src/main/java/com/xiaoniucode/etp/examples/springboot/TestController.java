package com.xiaoniucode.etp.examples.springboot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;


@Controller
@RequestMapping("/")
public class TestController {
    @Value("${spring.application.name}")
    private String appName;

    @RestController
    @RequestMapping("/api")
    public static class ApiController {
        @Value("${spring.application.name}")
        private String appName;

        @GetMapping("/hello")
        public String sayHello() {
            return "Hello " + appName;
        }
    }

    @GetMapping("")
    public ModelAndView index() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("forward:/index.html");
        return modelAndView;
    }
}
