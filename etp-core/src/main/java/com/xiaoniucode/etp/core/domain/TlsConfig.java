package com.xiaoniucode.etp.core.domain;

import io.netty.handler.ssl.ClientAuth;
import lombok.Getter;
import lombok.Setter;

public class TlsConfig {
    private Boolean enable = false;
    private String certFile;
    private String keyFile;
    private String caFile;

    public TlsConfig() {
    }
    public TlsConfig(Boolean enable, String certFile, String keyFile, String caFile) {
        this.enable = enable;
        this.certFile = certFile;
        this.keyFile = keyFile;
        this.caFile = caFile;
    }
    
    /**
     * 获取客户端认证模式
     * @return 客户端认证模式
     */
    public ClientAuth getClientAuthMode() {
        if (caFile != null && !caFile.isEmpty()) {
            return ClientAuth.REQUIRE;
        } else {
            return ClientAuth.NONE;
        }
    }
    
    /**
     * 是否启用双向认证
     * @return 是否启用双向认证
     */
    public boolean isMtlsEnabled() {
        return caFile != null && !caFile.isEmpty();
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public String getCertFile() {
        return certFile;
    }

    public void setCertFile(String certFile) {
        this.certFile = certFile;
    }

    public String getKeyFile() {
        return keyFile;
    }

    public void setKeyFile(String keyFile) {
        this.keyFile = keyFile;
    }

    public String getCaFile() {
        return caFile;
    }

    public void setCaFile(String caFile) {
        this.caFile = caFile;
    }
}
