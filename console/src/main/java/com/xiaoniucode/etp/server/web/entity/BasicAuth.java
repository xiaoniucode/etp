package com.xiaoniucode.etp.server.web.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "basic_auth")
@Setter
@Getter
public class BasicAuth {
    @Id
    @Column(name = "proxy_id")
    private String proxyId;

    @Column(name = "enable")
    private Boolean enable;

    public BasicAuth() {
    }

    public BasicAuth(String proxyId, Boolean enable) {
        this.proxyId = proxyId;
        this.enable = enable;
    }
}
