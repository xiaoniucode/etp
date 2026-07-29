package io.github.lxien.orbien.server.web.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * OAuth账号绑定
 */
@Data
@Entity
@Table(name = "oauth_binding",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_oauth_binding_external", columnNames = {"provider", "external_id"}),
                @UniqueConstraint(name = "uk_oauth_binding_user", columnNames = {"username", "provider"})
        })
public class OAuthBindingDO {
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * OAuth提供商
     */
    @Column(name = "provider", nullable = false, length = 32)
    private String provider;
    /**
     * 外部用户ID
     */
    @Column(name = "external_id", nullable = false, length = 128)
    private String externalId;
    /**
     * 外部登录名
     */
    @Column(name = "external_login", length = 255)
    private String externalLogin;
    /**
     * 本地用户名
     */
    @Column(name = "username", nullable = false, length = 128)
    private String username;
    /**
     * 绑定时间
     */
    @CreationTimestamp
    @Column(name = "bound_at", nullable = false)
    private LocalDateTime boundAt;
}
