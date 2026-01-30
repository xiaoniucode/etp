package com.xiaoniucode.etp.server.web.controller.proxy.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteProxyRequest {

    @NotNull(message = "代理ID不能为空")
    private Integer id;

}
