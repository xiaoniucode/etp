package cn.xilio.etp.server.store;

import java.io.Serializable;
import java.util.List;

/**
 * 客户端
 */
public class ClientInfo implements Serializable {
    /**
     * 客户端名称
     */
    private String name;
    /**
     * 客户端密钥，用于与代理服务器通信认证
     */
    private String secretKey;
    /**
     * 客户端的状态。1:在线，0:离线
     */
    private int status;
    /**
     * 代理映射信息，一个内网服务端口对应一个外网服务端口
     */
    private List<ProxyMapping> proxyMappings;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<ProxyMapping> getProxyMappings() {
        return proxyMappings;
    }

    public void setProxyMappings(List<ProxyMapping> proxyMappings) {
        this.proxyMappings = proxyMappings;
    }
}
