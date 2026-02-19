package com.xiaoniucode.etp.server.web.controller.accesscontrol.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 访问控制DTO
 */
@Getter
@Setter
public class AccessControlDTO {
    private Integer id;
    private String proxyId;
    private Boolean enable;
    private Integer mode;
    private List<AccessControlRuleDTO> rules;
}
