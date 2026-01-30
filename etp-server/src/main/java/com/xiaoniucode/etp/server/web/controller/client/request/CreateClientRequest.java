package com.xiaoniucode.etp.server.web.controller.client.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateClientRequest {

    @NotBlank(message = "客户端名称不能为空")
    private String name;

}
