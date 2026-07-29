package io.github.lxien.orbien.server.web.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * OAuth提供商配置
 */
@Data
@Entity
@Table(name = "oauth_provider",
        uniqueConstraints = @UniqueConstraint(name = "uk_oauth_provider", columnNames = "provider"))
public class OAuthProviderDO {
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 提供商标识
     */
    @Column(name = "provider", nullable = false, length = 32)
    private String provider;
    /**
     * 是否启用
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = false;
    /**
     * 客户端ID
     */
    @Column(name = "client_id")
    private String clientId;
    /**
     * 加密后的客户端密钥
     */
    @Column(name = "client_secret_enc", columnDefinition = "TEXT")
    private String clientSecretEnc;
    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
