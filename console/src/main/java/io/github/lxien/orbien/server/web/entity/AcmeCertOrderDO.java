package io.github.lxien.orbien.server.web.entity;

import io.github.lxien.orbien.server.web.entity.converter.AcmeOrderStatusConverter;
import io.github.lxien.orbien.server.web.entity.converter.AcmeValidationModeConverter;
import io.github.lxien.orbien.server.web.entity.converter.DnsProviderTypeConverter;
import io.github.lxien.orbien.server.web.enums.AcmeOrderStatus;
import io.github.lxien.orbien.server.web.enums.AcmeValidationMode;
import io.github.lxien.orbien.server.web.enums.DnsProviderType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * ACME TLS证书申请订单
 */
@Data
@Entity
@Table(name = "acme_cert_order")
public class AcmeCertOrderDO {
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 订单号
     */
    @Column(name = "order_no", nullable = false, unique = true)
    private String orderNo;

    /**
     * 订单状态
     */
    @Convert(converter = AcmeOrderStatusConverter.class)
    @Column(nullable = false)
    private AcmeOrderStatus status = AcmeOrderStatus.DRAFT;

    /**
     * 申请域名（JSON数组）
     */
    @Column(name = "domains", nullable = false, columnDefinition = "TEXT")
    private String domains;

    /**
     * 域名验证方式
     */
    @Convert(converter = AcmeValidationModeConverter.class)
    @Column(name = "validation_mode", nullable = false)
    private AcmeValidationMode validationMode;

    /**
     * DNS凭证ID
     */
    @Column(name = "dns_credential_id")
    private Long dnsCredentialId;

    /**
     * DNS服务商类型
     */
    @Convert(converter = DnsProviderTypeConverter.class)
    @Column(name = "dns_provider")
    private DnsProviderType dnsProvider;

    /**
     * ACME订单URL
     */
    @Column(name = "acme_order_url", columnDefinition = "TEXT")
    private String acmeOrderUrl;

    /**
     * 签发后关联的证书ID
     */
    @Column(name = "cert_id")
    private String certId;

    /**
     * 绑定的代理域名ID列表（JSON数组）
     */
    @Column(name = "bind_proxy_domain_ids", columnDefinition = "TEXT")
    private String bindProxyDomainIds;

    /**
     * 错误码
     */
    @Column(name = "error_code")
    private String errorCode;

    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 是否自动续期
     */
    @Column(name = "auto_renew")
    private Boolean autoRenew = false;

    /**
     * 证书过期时间
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

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
