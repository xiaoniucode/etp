package com.xiaoniucode.etp.server.config;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 客户端信息
 *
 * @author liuxin
 */
public class ClientInfo {
    /**
     * 客户端名字
     */
    private String name;
    /**
     * 客户端认证密钥
     */
    private String secretKey;
    /**
     * 客户端ID
     */
    private Integer clientId;
    /**
     * 存储客户端所有的端口映射信息
     */
    private List<ProxyMapping> proxies = new CopyOnWriteArrayList<>();

    public ClientInfo(String secretKey) {
        this.secretKey = secretKey;
    }

    public ClientInfo(String name, String secretKey) {
        this.name = name;
        this.secretKey = secretKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public List<ProxyMapping> getProxies() {
        return proxies;
    }

    public void setProxies(List<ProxyMapping> proxies) {
        this.proxies = proxies;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

