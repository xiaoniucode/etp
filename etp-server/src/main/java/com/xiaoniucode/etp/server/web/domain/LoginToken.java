package com.xiaoniucode.etp.server.web.domain;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 管理界面登陆认证令牌
 */
@Entity
@Table(name = "auth_tokens")
@Getter
@Setter
public class LoginToken {
    @Id
    @Column(name = "token", nullable = false)
    private String token;
    
    @Column(name = "uid", nullable = false)
    private Integer uid;
    
    @Column(name = "username", nullable = false)
    private String username;
    
    @Column(name = "expires_at", nullable = false)
    private Long expiresAt;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
