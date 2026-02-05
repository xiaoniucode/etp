package com.xiaoniucode.etp.server.web.manager;

public record CaptchaEntry(String code, long expireAt) {
}