package io.github.lxien.orbien.autoconfigure;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * HTTPS 代理访客侧 TLS 证书
 */
@Data
public class HttpsProxyTlsProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 私钥 PEM 文件路径
     */
    private String keyFile;
    /**
     * 证书链 PEM 文件路径
     */
    private String certFile;

}
