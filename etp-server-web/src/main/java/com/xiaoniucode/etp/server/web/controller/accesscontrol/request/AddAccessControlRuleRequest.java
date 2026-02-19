package com.xiaoniucode.etp.server.web.controller.accesscontrol.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddAccessControlRuleRequest {
    @NotNull(message = "访问控制ID不能为空")
    private Integer acId;
    @NotBlank(message = "CIDR 不能为空")
    private String cidr;
    @NotNull(message = "规则类型不能为空")
    private Integer ruleType;
}
