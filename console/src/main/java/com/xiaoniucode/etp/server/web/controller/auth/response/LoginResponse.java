package com.xiaoniucode.etp.server.web.controller.auth.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
@Getter
@Setter
@ToString
@AllArgsConstructor
public class LoginResponse implements Serializable {
    private String token;
}
