package com.xiaoniucode.etp.server.web.controller.accesscontrol.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 访问控制规则DTO
 */
@Getter
@Setter
public class AccessControlRuleDTO {
    private Integer id;
    private Integer acId;
    private String cidr;
    private Integer ruleType;
}
