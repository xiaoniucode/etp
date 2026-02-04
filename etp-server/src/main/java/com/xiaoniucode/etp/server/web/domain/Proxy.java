package com.xiaoniucode.etp.server.web.domain;

import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.core.enums.ProxyStatus;
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
    private String clientId;

    @Column(name = "name", nullable = false)
    private String name;

    @Convert(converter = ProtocolTypeConverter.class)
    @Column(name = "protocol", nullable = false)
    private ProtocolType protocol;

    @Column(name = "source", nullable = false)
    private Integer source;

    @Column(name = "local_ip", nullable = false)
    private String localIp;

    @Column(name = "local_port", nullable = false)
    private Integer localPort;

    @Column(name = "remote_port")
    private Integer remotePort;

    @Column(name = "status", nullable = false)
    @Convert(converter = ProxyStatusConverter.class)
    private ProxyStatus status;

    @Column(name = "domain_type")
    private Integer domainType;
    /**
     * 是否加密
     */
    @Column(name = "encrypt", nullable = false)
    private Boolean encrypt;
    /**
     * 是否压缩
     */
    @Column(name = "compress", nullable = false)
    private Boolean compress;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
