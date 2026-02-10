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
     * proxyId --> proxyConfig
     */
    private final Map<String, ProxyConfig> proxyToProxyConfig = new ConcurrentHashMap<>();

  public void addProxy(ProxyConfig config){
      proxyToProxyConfig.put(config.getProxyId(),config);
  }
    public boolean exist(String proxyId){
        return proxyToProxyConfig.containsKey(proxyId);
    }
}
