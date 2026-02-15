package com.xiaoniucode.etp.server.config.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TLSConfig {
    private boolean enable;
    private String certPath;
    private String keyPath;
    private String storePassPath;

    public TLSConfig(boolean enable) {
        this.enable = enable;
    }
    public TLSConfig(boolean enable, String certFile, String keyFile, String caFile) {
        this.enable = enable;
        this.certPath = certFile;
        this.keyPath = keyFile;
        this.storePassPath = caFile;
    }
}