package com.xiaoniucode.etp.server.manager.port;

import java.time.LocalDateTime;

/**
 * 端口上下文信息
 */
public class PortContext {
    private String proxyName;
    private int port;
    private boolean closed;
    private LocalDateTime updateTime;

    public PortContext(String proxyName, int port) {
        this.proxyName = proxyName;
        this.port = port;
        this.closed = false;
        this.updateTime = LocalDateTime.now();
    }

    public String getProxyName() {
        return proxyName;
    }

    public void setProxyName(String proxyName) {
        this.proxyName = proxyName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}