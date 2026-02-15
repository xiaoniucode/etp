package com.xiaoniucode.etp.client.config.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TlsConfig {
    private Boolean enable;
    private String certFile;
    private String keyFile;
    private String caFile;

    public TlsConfig(Boolean enable) {
        this.enable = enable;
    }

    public TlsConfig(Boolean enable, String certFile, String keyFile, String caFile) {
        this.enable = enable;
        this.certFile = certFile;
        this.keyFile = keyFile;
        this.caFile = caFile;
    }
}
