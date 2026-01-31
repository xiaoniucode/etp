package com.xiaoniucode.etp.core.domain;

import com.xiaoniucode.etp.common.utils.StringUtils;

public class LanInfo {
    private String localIP;
    private Integer localPort;

    public LanInfo(String localIP, Integer localPort) {
        check(localIP, localPort);
        this.localIP = localIP;
        this.localPort = localPort;
    }

    private void check(String localIP, Integer localPort) {
        if (!StringUtils.hasText(localIP)) {
            throw new NullPointerException("localIp不能为空");
        }
        if (localPort == null) {
            throw new NullPointerException("localPort不能为空");
        }
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
