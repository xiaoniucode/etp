package com.xiaoniucode.etp.server.web.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 代理域名实体类
 */
@Entity
@Table(name = "proxy_domains", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"domain"})
})
@Getter
@Setter
public class ProxyDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "proxy_id", nullable = false)
    private Integer proxyId;
    
    @Column(name = "domain", nullable = false)
    private String domain;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}
