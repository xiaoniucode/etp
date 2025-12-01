package cn.xilio.etp.server.config;

import cn.xilio.etp.core.protocol.ProtocolType;

/**
 * 端口映射信息
 *
 * @author liuxin
 */
public class ProxyMapping {
    /**
     * 端口映射ID，管理面板使用
     */
    private Integer proxyId;
    /**
     * 代理名称 自定义
     */
    private String name;
    /**
     * 协议 {@link ProtocolType}
     */
    private ProtocolType type;
    /**
     * 内网被代理服务的IP地址
     */
    private Integer localPort;
    /**
     * 公网服务端口，在对应IP范围内具备全局唯一性
     */
    private Integer remotePort;
    /**
     * 状态：1表示正常开启、0表示关闭，用户无法连接
     */
    private Integer status;

    public ProxyMapping(ProtocolType type, Integer localPort, Integer remotePort) {
        this.type = type;
        this.localPort = localPort;
        this.remotePort = remotePort;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProtocolType getType() {
        return type;
    }

    public void setType(ProtocolType type) {
        this.type = type;
    }

    public Integer getLocalPort() {
        return localPort;
    }

    public void setLocalPort(Integer localPort) {
        this.localPort = localPort;
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

    public Integer getProxyId() {
        return proxyId;
    }

    public void setProxyId(Integer proxyId) {
        this.proxyId = proxyId;
    }
}
