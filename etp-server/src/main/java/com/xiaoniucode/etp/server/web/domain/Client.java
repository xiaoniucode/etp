package com.xiaoniucode.etp.server.web.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "token", nullable = false)
    private String token;
    @Column(name = "os", nullable = false)
    private String os;
    @Column(name = "arch", nullable = false)
    private String arch;
    @Column(name = "version", nullable = false)
    private String version;
    @Column(name = "status", nullable = false)
    private Integer status;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
