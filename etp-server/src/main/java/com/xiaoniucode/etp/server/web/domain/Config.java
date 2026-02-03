package com.xiaoniucode.etp.server.web.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统配置实体类
 */
@Entity
@Table(name = "settings")
@Getter
@Setter
public class Config {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "config_key", nullable = false, unique = true)
    private String configKey;
    
    @Column(name = "config_value", nullable = false)
    private String configValue;

}
