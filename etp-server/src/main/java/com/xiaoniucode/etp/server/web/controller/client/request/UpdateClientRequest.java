package com.xiaoniucode.etp.server.web.controller.client.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateClientRequest {

    @NotNull(message = "客户端ID不能为空")
    private Integer id;

    @NotBlank(message = "客户端名称不能为空")
    private String name;

}
