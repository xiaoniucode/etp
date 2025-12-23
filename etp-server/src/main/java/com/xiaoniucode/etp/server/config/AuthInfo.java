package com.xiaoniucode.etp.server.config;

/**
 * 已认证客户端信息
 *
 * @author liuxin
 */
public class AuthInfo {
    private String arch;
    private String os;

    public AuthInfo(String os, String arch) {
        this.os = os;
        this.arch = arch;
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
