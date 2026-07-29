package io.github.lxien.orbien.server.web.entity;

import io.github.lxien.orbien.server.web.entity.converter.DnsCredentialStatusConverter;
import io.github.lxien.orbien.server.web.entity.converter.DnsProviderTypeConverter;
import io.github.lxien.orbien.server.web.enums.DnsCredentialStatus;
import io.github.lxien.orbien.server.web.enums.DnsProviderType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * DNS服务商凭证
 */
@Data
@Entity
@Table(name = "dns_credential")
public class DnsCredentialDO {
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 凭证名称
     */
    @Column(nullable = false)
    private String name;
    /**
     * DNS服务商类型
     */
    @Convert(converter = DnsProviderTypeConverter.class)
    @Column(nullable = false)
    private DnsProviderType provider;
    /**
     * 凭证配置JSON
     */
    @Column(name = "config_json", nullable = false, columnDefinition = "TEXT")
    private String configJson;
    /**
     * 凭证状态
     */
    @Convert(converter = DnsCredentialStatusConverter.class)
    @Column(nullable = false)
    private DnsCredentialStatus status = DnsCredentialStatus.UNTESTED;
    /**
     * 账号提示信息
     */
    @Column(name = "account_hint")
    private String accountHint;
    /**
     * 最近测试时间
     */
    @Column(name = "last_test_at")
    private LocalDateTime lastTestAt;
    /**
     * 最近测试结果
     */
    @Column(name = "last_test_message")
    private String lastTestMessage;
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
