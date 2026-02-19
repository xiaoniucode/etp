package com.xiaoniucode.etp.server.web.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 代理域名实体类
 */
@Entity
@Table(name = "proxy_domain")
@Getter
@Setter
public class ProxyDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "proxy_id", nullable = false)
    private String proxyId;
    @Column(name = "baseDomain")
    private String baseDomain;
    @Column(name = "domain", nullable = false)
    private String domain;
    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}
