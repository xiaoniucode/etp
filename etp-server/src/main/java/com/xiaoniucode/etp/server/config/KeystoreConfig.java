package com.xiaoniucode.etp.server.config;

/**
 * 密钥库配置
 *
 * @author liuxin
 */
public class KeystoreConfig {
    private String path;
    private String keyPass;
    private String storePass;

    public KeystoreConfig() {
    }

    public KeystoreConfig(String path, String keyPass, String storePass) {
        this.path = path;
        this.keyPass = keyPass;
        this.storePass = storePass;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getKeyPass() {
        return keyPass;
    }

    public void setKeyPass(String keyPass) {
        this.keyPass = keyPass;
    }

    public String getStorePass() {
        return storePass;
    }

    public void setStorePass(String storePass) {
        this.storePass = storePass;
    }
}


