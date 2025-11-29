package cn.xilio.etp.server.web.dto;

import java.io.Serializable;

public class UpdateProxyStatusReq implements Serializable {
    private String secretKey;
    private Integer remotePort;
    private Integer status;

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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
