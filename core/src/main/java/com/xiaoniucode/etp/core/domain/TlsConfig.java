package com.xiaoniucode.etp.core.domain;

import io.netty.handler.ssl.ClientAuth;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Builder
public class TlsConfig {
    private Boolean enable;
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
}
