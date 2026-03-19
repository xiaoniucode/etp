package com.xiaoniucode.etp.autoconfigure;

import lombok.Data;

@Data
public class TlsProperties {
    private Boolean enable = true;
    private String certFile;
    private String keyFile;
    private String caFile;
    private String keyPassword;
    /**
     * 自动生成TLS证书的测试模式，适用于测试环境，生产环境请勿启用，默认为开启状态
     */
    private boolean testMode = true;
}
