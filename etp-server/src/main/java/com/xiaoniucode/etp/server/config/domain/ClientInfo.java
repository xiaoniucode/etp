package com.xiaoniucode.etp.server.config.domain;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
@Builder
public class ClientInfo {
    private String clientId;
    private String name;
    /**
     * proxyName --> proxyConfig
     * 代理名称与代理配置映射
     */
    private final Map<String, ProxyConfig> proxyNameToProxyConfig = new ConcurrentHashMap<>();


    public boolean hasName(String name){
        return proxyNameToProxyConfig.containsKey(name);
    }
}
