package com.xiaoniucode.etp.server.web.controller.basicauth.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * HttpUser DTO
 */
@Getter
@Setter
public class HttpUserDTO {
    private Integer id;
    private String proxyId;
    private String user;
    private String pass;
}
