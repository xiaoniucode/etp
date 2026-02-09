package com.xiaoniucode.etp.server.web.controller.accesstoken.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
public class AccessTokenDTO {
    private Integer id;
    private String name;
    private String token;
    private Integer maxClient;
    private Integer onlineClient;
    private LocalDateTime createdAt;
}
