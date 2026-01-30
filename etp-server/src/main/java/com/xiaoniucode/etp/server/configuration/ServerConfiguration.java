package com.xiaoniucode.etp.server.configuration;

import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.server.generator.SessionIdGenerator;
import com.xiaoniucode.etp.server.proxy.HttpProxyServer;
import com.xiaoniucode.etp.server.proxy.TcpProxyServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServerConfiguration {
    @Bean
    public EventBus eventBus() {
        return new EventBus(16384);
    }

    @Bean
    public TcpProxyServer tcpProxyServer() {
        return new TcpProxyServer();
    }

    @Bean
    public HttpProxyServer httpProxyServer() {
        return new HttpProxyServer();
    }

    @Bean
    public SessionIdGenerator sessionIdGenerator() {
        return new SessionIdGenerator();
    }
}
