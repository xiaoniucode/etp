package com.xiaoniucode.etp.server.manager.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccessTokenInfo {
    private String token;
    private Integer maxClient;
}
