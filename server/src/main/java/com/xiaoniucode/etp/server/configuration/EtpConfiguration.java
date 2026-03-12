package com.xiaoniucode.etp.server.configuration;

import com.xiaoniucode.etp.core.notify.EventBus;
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class EtpConfiguration {
    @Bean
    public EventBus eventBus() {
        return new EventBus(16384);
    }
    @Bean
    public SpringContextHolder springContextHolder() {
        return new SpringContextHolder();
    }
    @Bean(destroyMethod = "stop")
    public HashedWheelTimer hashedWheelTimer() {
        return new HashedWheelTimer(
                new DefaultThreadFactory("wheel-timer"),
                100, // tick 时长
                TimeUnit.MILLISECONDS,// 时间单位
                512,  // 槽位数
                true  // 内存泄漏检测
        );
    }
}
