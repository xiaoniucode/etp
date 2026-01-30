package com.xiaoniucode.etp.core;

import com.xiaoniucode.etp.common.utils.StringUtils;

/**
 * @author liuxin
 */
public class AuthClientInfo {
    private String token;
    private String arch;
    private String os;

    public AuthClientInfo(String token, String arch, String os) {
        check(token);
        this.token = token;
        this.arch = arch;
        this.os = os;
    }
    private void check(String token) {
        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("token不能为空");
        }
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getArch() {
        return arch;
    }

    public void setArch(String arch) {
        this.arch = arch;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }
}