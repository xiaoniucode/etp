package com.xiaoniucode.etp.server.web.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
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
    
    @Column(name = "source", nullable = false, columnDefinition = "DEFAULT 0")
    private Integer source;
    
    @Column(name = "local_ip", nullable = false)
    private String localIp;
    
    @Column(name = "local_port", nullable = false)
    private Integer localPort;
    
    @Column(name = "remote_port")
    private Integer remotePort;
    
    @Column(name = "status", nullable = false, columnDefinition = "DEFAULT 1")
    private Integer status;
    
    @Column(name = "domain_type")
    private Integer domainType;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
