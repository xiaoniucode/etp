package com.xiaoniucode.etp.server.config.domain;

import com.xiaoniucode.etp.core.codec.ProtocolType;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class ClientInfo {
    private String name;
    private String secretKey;
    private Integer clientId;
    private List<ProxyConfig> proxies = new CopyOnWriteArrayList<>();

    public ClientInfo(String name, String secretKey, Integer clientId) {
        this.name = name;
        this.secretKey = secretKey;
        this.clientId = clientId;
    }

    public ClientInfo(String name, String secretKey, Integer clientId, List<ProxyConfig> proxies) {
        this.name = name;
        this.secretKey = secretKey;
        this.clientId = clientId;
        this.proxies = proxies;
    }

    public String getName() {
        return name;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public Integer getClientId() {
        return clientId;
    }

    public List<ProxyConfig> getTcpProxies() {
        return proxies.stream().filter(f -> f.getType() == ProtocolType.TCP).collect(Collectors.toList());
    }

    public Set<ProxyConfig> getHttpProxies() {
        return proxies.stream().filter(f -> f.getType() == ProtocolType.HTTP).collect(Collectors.toSet());
    }

    public List<ProxyConfig> getProxies() {
        return proxies;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public void setProxies(List<ProxyConfig> proxies) {
        this.proxies = proxies;
    }
}
