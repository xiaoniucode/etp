package com.xiaoniucode.etp.server.web.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 用于客户端连接代理服务器访问认证
 */
@Entity
@Table(name = "access_tokens")
@Getter
@Setter
public class AccessToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "token", nullable = false)
    private String token;

    @Column(name = "max_client", nullable = false)
    private Integer maxClient = 0;
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public AccessToken() {
    }

    public AccessToken(String name, String token, Integer maxClient) {
        this.name = name;
        this.token = token;
        this.maxClient = maxClient;
    }
}
