package com.xiaoniucode.etp.server.web.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 用于客户端连接代理服务器访问认证
 */
@Entity
@Table(name = "access_tokens", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"token"})
})
@Getter
@Setter
public class AccessToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "token", nullable = false)
    private String token;
    
    @Column(name = "max_client")
    private Integer maxClient;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
//
//    @Column(name = "expires_at", nullable = false)
//    private LocalDateTime expiresAt;
}
