package cn.xilio.etp.server.store;

import java.io.Serializable;

/**
 * 内网服务信息
 */
public class LocalServerInfo implements Serializable {
    /**
     * 内网服务IP地址
     */
    private String localIp;
    /**
     * 内网服务端口号
     */
    private Integer localPort;

    public LocalServerInfo(String localIp, Integer localPort) {
        this.localIp = localIp;
        this.localPort = localPort;
    }

    public String getLocalIp() {
        return localIp;
    }

    public void setLocalIp(String localIp) {
        this.localIp = localIp;
    }

    public Integer getLocalPort() {
        return localPort;
    }

    public void setLocalPort(Integer localPort) {
        this.localPort = localPort;
    }
}
