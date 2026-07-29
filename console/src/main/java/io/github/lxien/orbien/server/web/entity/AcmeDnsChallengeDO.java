package io.github.lxien.orbien.server.web.entity;

import io.github.lxien.orbien.server.web.entity.converter.AcmeChallengeStatusConverter;
import io.github.lxien.orbien.server.web.enums.AcmeChallengeStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * ACME DNS-01验证挑战记录
 */
@Data
@Entity
@Table(name = "acme_dns_challenge")
public class AcmeDnsChallengeDO {
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联订单ID
     */
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    /**
     * 待验证域名
     */
    @Column(nullable = false)
    private String domain;

    /**
     * DNS记录完整名称
     */
    @Column(name = "record_name", nullable = false)
    private String recordName;

    /**
     * 主机记录（相对区域的前缀）
     */
    @Column(name = "host_record")
    private String hostRecord;

    /**
     * DNS区域
     */
    @Column(name = "dns_zone")
    private String dnsZone;

    /**
     * 厂商API使用的区域标识
     */
    @Column(name = "provider_zone")
    private String providerZone;

    /**
     * DNS记录值
     */
    @Column(name = "record_value", nullable = false, columnDefinition = "TEXT")
    private String recordValue;

    /**
     * DNS记录类型
     */
    @Column(name = "record_type", nullable = false)
    private String recordType = "TXT";

    /**
     * 厂商侧DNS记录ID
     */
    @Column(name = "provider_record_id")
    private String providerRecordId;

    /**
     * ACME挑战URL
     */
    @Column(name = "challenge_url", columnDefinition = "TEXT")
    private String challengeUrl;

    /**
     * 挑战状态
     */
    @Convert(converter = AcmeChallengeStatusConverter.class)
    @Column(nullable = false)
    private AcmeChallengeStatus status = AcmeChallengeStatus.PENDING;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 验证通过时间
     */
    @Column(name = "validated_at")
    private LocalDateTime validatedAt;
}
