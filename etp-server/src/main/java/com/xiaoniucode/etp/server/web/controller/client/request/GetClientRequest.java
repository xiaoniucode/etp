package com.xiaoniucode.etp.server.web.controller.client.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetClientRequest {

    @NotNull(message = "客户端ID不能为空")
    private Integer id;

}
