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
     * name --> proxyConfig
     */
    private final Map<String, ProxyConfig> proxyNameToProxyConfig = new ConcurrentHashMap<>();

    public void addProxy(ProxyConfig config) {
        proxyNameToProxyConfig.put(config.getName(), config);
    }

    /**
     * 通过代理名字删除代理配置
     *
     * @param name 代理配置名称
     */
    public void removeProxyByName(String name) {
        proxyNameToProxyConfig.remove(name);
    }

    public ProxyConfig getProxyConfig(String name) {
        if (name == null) {
            return null;
        }
        return proxyNameToProxyConfig.get(name);
    }
}
