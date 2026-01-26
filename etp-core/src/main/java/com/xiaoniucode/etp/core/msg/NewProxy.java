package com.xiaoniucode.etp.core.msg;

import com.xiaoniucode.etp.core.codec.ProtocolType;

import java.util.Set;

public class NewProxy implements Message{
    private String name;
    private String localIP;
    private Integer localPort;
    private ProtocolType protocol;
    private Integer remotePort;
    private Integer status;
    private Set<String> customDomains;
    private Set<String>subDomains;
    private Boolean autoDomain;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocalIP() {
        return localIP;
    }

    public void setLocalIP(String localIP) {
        this.localIP = localIP;
    }

    public Integer getLocalPort() {
        return localPort;
    }

    public void setLocalPort(Integer localPort) {
        this.localPort = localPort;
    }

    public ProtocolType getProtocol() {
        return protocol;
    }

    public void setProtocol(ProtocolType protocol) {
        this.protocol = protocol;
    }

    public Integer getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(Integer remotePort) {
        this.remotePort = remotePort;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Set<String> getCustomDomains() {
        return customDomains;
    }

    public void setCustomDomains(Set<String> customDomains) {
        this.customDomains = customDomains;
    }

    public Set<String> getSubDomains() {
        return subDomains;
    }

    public void setSubDomains(Set<String> subDomains) {
        this.subDomains = subDomains;
    }

    public Boolean getAutoDomain() {
        return autoDomain;
    }

    public void setAutoDomain(Boolean autoDomain) {
        this.autoDomain = autoDomain;
    }

    @Override
    public byte getType() {
        return Message.TYPE_NEW_PROXY;
    }
}
