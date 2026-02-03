package com.xiaoniucode.etp.server.web.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 代理实体类
 */
@Entity
@Table(name = "proxies")
@Getter
@Setter
public class Proxy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "client_id", nullable = false)
    private Integer clientId;
    
    @Column(name = "name", nullable = false, unique = true)
    private String name;
    
    @Column(name = "type", nullable = false)
    private String type;
    
    @Column(name = "source", nullable = false)
    private Integer source;
    
    @Column(name = "local_ip", nullable = false)
    private String localIp;
    
    @Column(name = "local_port", nullable = false)
    private Integer localPort;
    
    @Column(name = "remote_port")
    private Integer remotePort;
    
    @Column(name = "status", nullable = false)
    private Integer status;
    
    @Column(name = "domain_type")
    private Integer domainType;
    
    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
