package com.xiaoniucode.etp.server.web.controller.accesstoken.request;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class BatchDeleteAccessTokenRequest {
    @NotNull(message = "ids 不能为空")
    private List<Integer> ids;
}
