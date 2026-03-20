package com.xiaoniucode.etp.server.registry;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProxyOperationDelegateFactory {
    private final List<ProxyOperationDelegate> delegates;
    
    @Autowired
    public ProxyOperationDelegateFactory(List<ProxyOperationDelegate> delegates) {
        this.delegates = delegates;
    }
    
    public ProxyOperationDelegate getDelegate(ProxyConfig config) {
        return delegates.stream()
            .filter(delegate -> delegate.supports(config))
            .findFirst()
            .orElseThrow(() -> new UnsupportedOperationException("不支持的代理类型: " + config.getClass()));
    }
}