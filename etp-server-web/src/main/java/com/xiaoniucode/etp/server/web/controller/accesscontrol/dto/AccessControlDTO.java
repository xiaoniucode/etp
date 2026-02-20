package com.xiaoniucode.etp.server.web.controller.accesscontrol.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AccessControlDTO {
    private String proxyId;
    private Boolean enable;
    private Integer mode;
    private List<AccessControlRuleDTO> rules;
}
