package com.xiaoniucode.etp.server.web.domain;

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
    /**
     * 设备ID
     */
    @Id
    @Column(name = "id", nullable = false)
    private String id;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "os", nullable = false)
    private String os;
    @Column(name = "arch", nullable = false)
    private String arch;
    @Column(name = "version", nullable = false)
    private String version;
    @Column(name = "status", nullable = false)
    private Integer status;
    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
