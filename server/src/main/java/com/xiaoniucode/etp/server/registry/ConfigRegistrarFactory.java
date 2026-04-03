package com.xiaoniucode.etp.server.registry;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConfigRegistrarFactory {
    private final List<ConfigRegistrar> registrars;
    
    @Autowired
    public ConfigRegistrarFactory(List<ConfigRegistrar> registrars) {
        this.registrars = registrars;
    }
    
    public ConfigRegistrar getRegistrar(ProxyConfig config) {
        return registrars.stream()
            .filter(delegate -> delegate.supports(config))
            .findFirst()
            .orElseThrow(() -> new UnsupportedOperationException("不支持的代理类型: " + config.getClass()));
    }
}