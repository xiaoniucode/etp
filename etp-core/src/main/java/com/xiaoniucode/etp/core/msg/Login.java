package com.xiaoniucode.etp.core.msg;

public class Login implements Message{
    private String os;
    private String arch;
    private String secretKey;

    public Login(String secretKey, String os, String arch) {
        this.secretKey = secretKey;
        this.os = os;
        this.arch = arch;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getArch() {
        return arch;
    }

    public void setArch(String arch) {
        this.arch = arch;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    @Override
    public char getType() {
        return Message.TYPE_LOGIN;
    }
}
