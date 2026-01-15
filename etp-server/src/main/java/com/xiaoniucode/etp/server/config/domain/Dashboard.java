package com.xiaoniucode.etp.server.config.domain;

public class Dashboard {
    private Boolean enable;
    private String username;
    private String password;
    private String addr;
    private Integer port;
    private Boolean reset;

    public Dashboard(Boolean enable) {
        this.enable = enable;
    }

    public Dashboard(Boolean enable, String username, String password, String addr, Integer port, Boolean reset) {
        this.enable = enable;
        this.username = username;
        this.password = password;
        this.addr = addr;
        this.port = port;
        this.reset = reset;
    }

    public Boolean getEnable() {
        return enable;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getAddr() {
        return addr;
    }

    public Integer getPort() {
        return port;
    }

    public Boolean getReset() {
        return reset;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setReset(Boolean reset) {
        this.reset = reset;
    }
}
