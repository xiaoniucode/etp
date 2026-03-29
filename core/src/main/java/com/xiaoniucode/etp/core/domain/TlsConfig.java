package com.xiaoniucode.etp.core.domain;

import io.netty.handler.ssl.ClientAuth;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
public class TlsConfig {
    private Boolean enabled;
    private String certFile;
    private String keyFile;
    private String caFile;
    private String keyPassword;
    private boolean testMode;

    public TlsConfig(boolean enabled, boolean isTestMode) {
        this.enabled = enabled;
        this.testMode=isTestMode;
    }
    public TlsConfig(Boolean enabled, String certFile, String keyFile, String caFile, String keyPassword, boolean testMode) {
        this.enabled = enabled;
        this.certFile = certFile;
        this.keyFile = keyFile;
        this.caFile = caFile;
        this.keyPassword=keyPassword;
        this.testMode=testMode;
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
    public boolean mTLSEnabled() {
        return caFile != null && !caFile.isEmpty();
    }
}
