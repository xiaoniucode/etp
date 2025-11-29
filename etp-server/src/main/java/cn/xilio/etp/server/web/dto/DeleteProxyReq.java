package cn.xilio.etp.server.web.dto;

import java.io.Serializable;

public class DeleteProxyReq implements Serializable {
    private String secretKey;
    private Integer remotePort;

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public Integer getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(Integer remotePort) {
        this.remotePort = remotePort;
    }
}
