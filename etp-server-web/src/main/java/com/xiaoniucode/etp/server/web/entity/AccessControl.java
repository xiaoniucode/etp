package com.xiaoniucode.etp.server.web.entity;

import com.xiaoniucode.etp.core.enums.AccessControlMode;
import com.xiaoniucode.etp.server.web.entity.converter.AccessControlModeConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 访问控制实体类
 */
@Getter
@Setter
@Entity
@Table(name = "access_control")
public class AccessControl {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "proxy_id", nullable = false, unique = true)
    private String proxyId;
    
    @Column(name = "enable", nullable = false)
    private Boolean enable;
    
    @Column(name = "mode", nullable = false)
    @Convert(converter = AccessControlModeConverter.class)
    private AccessControlMode mode;
}
