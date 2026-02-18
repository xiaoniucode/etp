package com.xiaoniucode.etp.server.web.controller.accesstoken.request;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class BatchDeleteAccessTokenRequest {
    private List<Integer> ids;
}
