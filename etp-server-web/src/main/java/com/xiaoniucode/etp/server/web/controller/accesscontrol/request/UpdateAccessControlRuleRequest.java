package com.xiaoniucode.etp.server.web.controller.accesscontrol.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAccessControlRuleRequest {
    @NotNull(message = "ID不能为空")
    private Integer id;
    @NotNull(message = "规则类型不能为空")
    private Integer ruleType;
}
