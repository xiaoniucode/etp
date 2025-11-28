package cn.xilio.etp.server.store;

import cn.xilio.etp.core.protocol.ProtocolType;

import java.io.Serializable;

/**
 * 代理映射信息
 * @author liuxin
 */
public class ProxyMapping implements Serializable {
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
     * 远程服务端口
     */
    private Integer remotePort;
    /**
     * 状态：1表示正常开启、0表示关闭，用户无法连接
     */
    private Integer status;

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
}
