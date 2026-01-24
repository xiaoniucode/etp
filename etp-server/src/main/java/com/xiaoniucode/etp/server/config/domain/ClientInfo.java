package com.xiaoniucode.etp.server.config.domain;

import com.xiaoniucode.etp.common.utils.StringUtils;
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
        check(name, secretKey, clientId);
        this.name = name;
        this.secretKey = secretKey;
        this.clientId = clientId;
    }

    private void check(String name, String secretKey, Integer clientId) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("客户端名称不能为空");
        }
        if (!StringUtils.hasText(secretKey)) {
            throw new IllegalArgumentException("客户端密钥不能为空");
        }
        if (clientId == null) {
            throw new IllegalArgumentException("客户端ID不能为空");
        }
    }

    public ClientInfo(String name, String secretKey, Integer clientId, List<ProxyConfig> proxies) {
        check(name, secretKey, clientId);
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
