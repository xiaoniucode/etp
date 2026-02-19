package com.xiaoniucode.etp.server.web.entity;

import com.xiaoniucode.etp.core.enums.AccessControlMode;
import com.xiaoniucode.etp.server.web.entity.converter.AccessControlModeConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 访问控制规则实体类
 */
@Getter
@Setter
@Entity
@Table(name = "access_control_rule")
public class AccessControlRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "ac_id", nullable = false)
    private Integer acId;
    
    @Column(name = "cidr", nullable = false)
    private String cidr;
    
    @Column(name = "rule_type", nullable = false)
    @Convert(converter = AccessControlModeConverter.class)
    private AccessControlMode ruleType;
}
