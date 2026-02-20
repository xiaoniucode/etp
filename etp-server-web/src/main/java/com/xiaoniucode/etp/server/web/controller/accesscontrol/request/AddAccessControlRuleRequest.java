package com.xiaoniucode.etp.server.web.controller.accesscontrol.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddAccessControlRuleRequest {
    @NotEmpty(message = "代理proxyId 不能为空")
    private String proxyId;
    @NotBlank(message = "cidr 不能为空")
    private String cidr;
    @NotNull(message = "规则类型不能为空")
    private Integer ruleType;
}
