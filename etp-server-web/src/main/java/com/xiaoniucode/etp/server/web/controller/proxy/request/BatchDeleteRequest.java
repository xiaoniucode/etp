package com.xiaoniucode.etp.server.web.controller.proxy.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
public class BatchDeleteRequest implements Serializable {
    @NotNull(message = "ids 不能为空")
    private Set<String> ids;
}
