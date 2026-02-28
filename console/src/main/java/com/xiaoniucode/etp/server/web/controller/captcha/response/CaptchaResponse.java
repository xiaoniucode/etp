package com.xiaoniucode.etp.server.web.controller.captcha.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class CaptchaResponse {
    private String captchaId;
    private String code;
}
