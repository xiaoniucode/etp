package com.xiaoniucode.etp.server.web.domain;

import com.xiaoniucode.etp.core.enums.ClientType;
import com.xiaoniucode.etp.server.web.domain.converter.ClientTypeConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 客户端实体类
 */
@Entity
@Table(name = "clients")
@Getter
@Setter
public class Client {
    @Id
    @Column(name = "id", nullable = false)
    private String id;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "clientType", nullable = false)
    @Convert(converter = ClientTypeConverter.class)
    private ClientType clientType;
    @Column(name = "os", nullable = false)
    private String os;
    @Column(name = "arch", nullable = false)
    private String arch;
    @Column(name = "version", nullable = false)
    private String version;
    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
