package com.xiaoniucode.etp.core;

public class LanInfo {
    private String localIP;
    private Integer localPort;

    public LanInfo(String localIP, Integer localPort) {
        this.localIP = localIP;
        this.localPort = localPort;
    }

    public String getLocalIP() {
        return localIP;
    }

    public Integer getLocalPort() {
        return localPort;
    }

    public void setLocalIP(String localIP) {
        this.localIP = localIP;
    }

    public void setLocalPort(Integer localPort) {
        this.localPort = localPort;
    }
}
