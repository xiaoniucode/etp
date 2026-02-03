package com.xiaoniucode.etp.server.config.domain;

public class TLSConfig {
    private boolean enable;
    private String certPath;
    private String keyPath;
    private String storePassPath;

    public TLSConfig(boolean enable, String certPath, String keyPath, String storePassPath) {
        this.enable = enable;
        this.certPath = certPath;
        this.keyPath = keyPath;
        this.storePassPath = storePassPath;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getCertPath() {
        return certPath;
    }

    public void setCertPath(String certPath) {
        this.certPath = certPath;
    }

    public String getKeyPath() {
        return keyPath;
    }

    public void setKeyPath(String keyPath) {
        this.keyPath = keyPath;
    }

    public String getStorePassPath() {
        return storePassPath;
    }

    public void setStorePassPath(String storePassPath) {
        this.storePassPath = storePassPath;
    }
}