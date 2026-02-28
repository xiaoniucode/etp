package com.xiaoniucode.etp.server.web.controller.client.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class BatchDeleteClientRequest implements Serializable {
    @NotNull(message = "ids 不能为空")
    List<String> ids;
}
