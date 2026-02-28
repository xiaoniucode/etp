package com.xiaoniucode.etp.server.config.domain;

public class KeystoreConfig {
    private String path;
    private String keyPass;
    private String storePass;

    public KeystoreConfig(String path, String keyPass, String storePass) {
        this.path = path;
        this.keyPass = keyPass;
        this.storePass = storePass;
    }

    public String getPath() {
        return path;
    }

    public String getKeyPass() {
        return keyPass;
    }

    public String getStorePass() {
        return storePass;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setKeyPass(String keyPass) {
        this.keyPass = keyPass;
    }

    public void setStorePass(String storePass) {
        this.storePass = storePass;
    }
}
