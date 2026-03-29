package com.xiaoniucode.etp.server.config.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Dashboard {
    private Boolean enabled;
    private String username;
    private String password;
    private String addr;
    private Integer port;
    private Boolean reset;

    public Dashboard(Boolean enabled) {
        this.enabled = enabled;
    }

    public Dashboard(Boolean enabled, String username, String password, String addr, Integer port, Boolean reset) {
        this.enabled = enabled;
        this.username = username;
        this.password = password;
        this.addr = addr;
        this.port = port;
        this.reset = reset;
    }
}
