package cn.xilio.etp.server.config;

import java.io.Serializable;

/**
 * @author liuxin
 */
public class Dashboard implements Serializable {
    private Boolean enable = false;
    private String username;
    private String password;
    /**
     * 是否重置数据库中的登录信息，如果设置为true，如果用户之前登录过管理界面，用户信息将会重置
     */
    private Boolean reset = false;
    private String addr;
    private Integer port;

    public Dashboard() {
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

    public Boolean getReset() {
        return reset;
    }

    public void setReset(Boolean reset) {
        this.reset = reset;
    }
}
