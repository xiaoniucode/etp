package cn.xilio.etp.server.store;

import java.io.Serializable;

/**
 * @author liuxin
 */
public class Dashboard implements Serializable {
    private Boolean enable;
    private String username;
    private String password;
    private String addr;
    private Integer port;

    public Dashboard() {
    }

    public Dashboard(Boolean enable, String username, String password, String addr, Integer port) {
        this.enable = enable;
        this.username = username;
        this.password = password;
        this.addr = addr;
        this.port = port;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
