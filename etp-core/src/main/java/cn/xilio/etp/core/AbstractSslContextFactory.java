package cn.xilio.etp.core;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslProvider;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;

/**
 * SSL上下文工厂抽象父类
 * @author liuxin
 */
public abstract class AbstractSslContextFactory {
    // 强制采用TLS1.3协议版本
    protected static final String PROTOCOL_TLS_1_3 = "TLSv1.3";
    protected static final SslProvider DEFAULT_SSL_PROVIDER = SslProvider.JDK;

    /**
     * 校验证书文件是否存在且可读
     */
    protected void validateFile(File file, String desc) {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException(desc + "文件不存在：" + (file != null ? file.getAbsolutePath() : "null"));
        }
        if (!file.canRead()) {
            throw new IllegalArgumentException(desc + "文件不可读：" + file.getAbsolutePath());
        }
    }

    /**
     * 加载JKS格式的密钥库
     * @param keyStoreFile 密钥库文件
     * @param keyPass 私钥密码
     * @param storePass 存储库密码
     */
    protected KeyManagerFactory loadKeyStore(File keyStoreFile, String keyPass,String storePass) throws Exception {
        validateFile(keyStoreFile, "密钥库");
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream is = new FileInputStream(keyStoreFile)) {
            keyStore.load(is, storePass.toCharArray());
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, keyPass.toCharArray());
        return kmf;
    }

    /**
     * 加载JKS格式的信任库
     * @param trustStoreFile 信任库文件
     * @param password 信任库密码
     */
    protected TrustManagerFactory loadTrustStore(File trustStoreFile, String password) throws Exception {
        validateFile(trustStoreFile, "信任库");
        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream is = new FileInputStream(trustStoreFile)) {
            trustStore.load(is, password.toCharArray());
        }
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);
        return tmf;
    }

    /**
     * 子类需实现的抽象方法：创建具体的SslContext
     */
    public abstract SslContext createContext() throws Exception;
}
