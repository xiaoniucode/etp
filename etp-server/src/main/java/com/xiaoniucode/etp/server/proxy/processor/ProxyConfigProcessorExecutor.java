package com.xiaoniucode.etp.server.proxy.processor;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.enums.ProtocolType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProxyConfigProcessorExecutor {
    private final List<ProxyConfigProcessor> processors;
    
    public ProxyConfigProcessorExecutor(List<ProxyConfigProcessor> processors) {
        this.processors = processors;
    }
    
    public void execute(ProxyConfig proxyConfig) {
        ProtocolType protocol = proxyConfig.getProtocol();
        processors.stream()
            .filter(p -> p.supports(protocol))
            .findFirst()
            .ifPresent(p -> p.process(proxyConfig));
    }
}