package com.xiaoniucode.etp.server.config.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DashboardConfig {
    private Boolean enabled;
    private String username;
    private String password;
    private String addr;
    private Integer port;

    public DashboardConfig(Boolean enabled) {
        this.enabled = enabled;
    }

    public DashboardConfig(Boolean enabled, String username, String password, String addr, Integer port) {
        this.enabled = enabled;
        this.username = username;
        this.password = password;
        this.addr = addr;
        this.port = port;
    }
}
