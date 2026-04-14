package com.xiaoniucode.etp.server.configuration;

import com.baidu.fsg.uid.UidGenerator;
import com.baidu.fsg.uid.impl.CachedUidGenerator;
import com.baidu.fsg.uid.impl.DefaultUidGenerator;
import com.xiaoniucode.etp.core.notify.EventBus;
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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

    /**
     * 19位雪花算法生成
     * @return ID
     */
    @Bean
    @ConditionalOnMissingBean(UidGenerator.class)
    public UidGenerator uidGenerator() {
        CachedUidGenerator generator = new CachedUidGenerator();
        generator.setTimeBits(29);      // 时间差（秒）
        generator.setWorkerBits(21);    // workerId
        generator.setSeqBits(13);       // 序列号
        // 自定义起始时间
        generator.setEpochStr("2026-04-15");
        // 工作节点ID
        generator.setWorkerIdAssigner(() -> 1L);
        return generator;
    }
}
