package com.xiaoniucode.etp.server.web.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "http_user")
@Setter
@Getter
public class HttpUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "proxy_id")
    private String proxyId;

    @Column(name = "user")
    private String user;

    @Column(name = "pass")
    private String pass;

    public HttpUser() {
    }

    public HttpUser(String proxyId, String user, String pass) {
        this.proxyId = proxyId;
        this.user = user;
        this.pass = pass;
    }
}
