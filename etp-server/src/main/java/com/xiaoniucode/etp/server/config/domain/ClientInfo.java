package com.xiaoniucode.etp.server.config.domain;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import lombok.Getter;
import lombok.Setter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class ClientInfo {
    private String clientId;
    private String name;
    private String arch;
    private String os;
    private String version;
    /**
     * proxyName --> proxyConfig
     * 代理名称与代理配置映射
     */
    private final Map<String, ProxyConfig> proxyNameToProxyConfig = new ConcurrentHashMap<>();

}
