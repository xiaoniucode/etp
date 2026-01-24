package com.xiaoniucode.etp.core;

import com.xiaoniucode.etp.common.utils.StringUtils;

/**
 * @author liuxin
 */
public class AuthClientInfo {
    private String secretKey;
    private String arch;
    private String os;

    public AuthClientInfo(String secretKey, String arch, String os) {
        check(secretKey);
        this.secretKey = secretKey;
        this.arch = arch;
        this.os = os;
    }

    private void check(String secretKey) {
        if (!StringUtils.hasText(secretKey)) {
            throw new IllegalArgumentException("secretKey不能为空");
        }
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
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
