package com.xiaoniucode.etp.core.tls;

import com.xiaoniucode.etp.core.domain.TlsConfig;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * SSL上下文工厂，用于创建SSL上下文，支持OpenSSL证书
 */
public class SslContextFactory {

    /**
     * 创建服务器端SSL上下文
     * @param config TLS配置
     * @return SSL上下文
     * @throws Exception 异常
     */
    public static SslContext createServerSslContext(TlsConfig config) throws Exception {
        // 创建KeyManagerFactory，用于加载服务器证书和私钥
        KeyManagerFactory keyManagerFactory = createKeyManagerFactory(
                config.getCertFile(),
                config.getKeyFile()
        );

        SslContextBuilder builder = SslContextBuilder.forServer(keyManagerFactory);

        // 指定使用TLS 1.3协议
        builder.protocols("TLSv1.3");

        // 如果配置了CA文件，启用双向认证
        if (config.isMtlsEnabled()) {
            // 加载CA证书
            List<X509Certificate> caCerts = loadCaCerts(config.getCaFile());
            // 创建信任管理器工厂
            TrustManagerFactory trustManagerFactory = createTrustManagerFactory(caCerts);
            // 设置信任管理器和客户端认证模式
            builder.trustManager(trustManagerFactory)
                    .clientAuth(config.getClientAuthMode());
        } else {
            // 单向认证，不需要验证客户端证书
            builder.clientAuth(config.getClientAuthMode());
        }

        return builder.build();
    }

    /**
     * 创建客户端SSL上下文
     * @param config TLS配置
     * @return SSL上下文
     * @throws Exception 异常
     */
    public static SslContext createClientSslContext(TlsConfig config) throws Exception {
        SslContextBuilder builder = SslContextBuilder.forClient();

        // 指定使用TLS 1.3协议
        builder.protocols("TLSv1.3");

        // 如果配置了CA文件，验证服务器证书
        if (config.isMtlsEnabled()) {
            // 加载CA证书
            List<X509Certificate> caCerts = loadCaCerts(config.getCaFile());
            // 创建信任管理器工厂
            TrustManagerFactory trustManagerFactory = createTrustManagerFactory(caCerts);
            // 设置信任管理器
            builder.trustManager(trustManagerFactory);
        } else {
            // 使用系统默认的信任管理器，验证服务器证书
            // 这样即使不使用CA文件，也会验证服务器证书是否由系统信任的CA签发
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm()
            );
            trustManagerFactory.init((KeyStore) null);
            builder.trustManager(trustManagerFactory);
        }

        // 如果配置了客户端证书和密钥，启用客户端认证
        if (config.getCertFile() != null && !config.getCertFile().isEmpty() &&
                config.getKeyFile() != null && !config.getKeyFile().isEmpty()) {
            // 创建KeyManagerFactory，用于加载客户端证书和私钥
            KeyManagerFactory keyManagerFactory = createKeyManagerFactory(
                    config.getCertFile(),
                    config.getKeyFile()
            );
            builder.keyManager(keyManagerFactory);
        }

        return builder.build();
    }

    /**
     * 创建KeyManagerFactory，用于加载证书和私钥
     * @param certFile 证书文件路径
     * @param keyFile 私钥文件路径
     * @return KeyManagerFactory
     * @throws Exception 异常
     */
    private static KeyManagerFactory createKeyManagerFactory(String certFile, String keyFile) throws Exception {
        // 创建一个临时的PKCS12密钥库
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);

        // 加载证书
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        try (FileInputStream fis = new FileInputStream(certFile)) {
            X509Certificate cert = (X509Certificate) cf.generateCertificate(fis);
            keyStore.setCertificateEntry("cert", cert);
        }

        // 加载私钥（支持PKCS#1和PKCS#8格式）
        byte[] keyBytes = readPrivateKey(keyFile);
        java.security.PrivateKey privateKey = generatePrivateKey(keyBytes);

        // 将私钥和证书添加到密钥库
        keyStore.setKeyEntry("key", privateKey, "".toCharArray(), new java.security.cert.Certificate[]{
                keyStore.getCertificate("cert")
        });

        // 创建KeyManagerFactory
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm()
        );
        keyManagerFactory.init(keyStore, "".toCharArray());

        return keyManagerFactory;
    }

    /**
     * 读取私钥文件，支持PEM格式
     * @param keyFile 私钥文件路径
     * @return 私钥字节数组
     * @throws Exception 异常
     */
    private static byte[] readPrivateKey(String keyFile) throws Exception {
        try (FileInputStream fis = new FileInputStream(keyFile)) {
            byte[] allBytes = fis.readAllBytes();
            String keyContent = new String(allBytes);

            // 移除PEM格式的头部和尾部
            keyContent = keyContent.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                    .replace("-----END RSA PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            // 解码Base64
            return Base64.getDecoder().decode(keyContent);
        }
    }

    /**
     * 生成私钥，支持PKCS#1和PKCS#8格式
     * @param keyBytes 私钥字节数组
     * @return 私钥对象
     * @throws Exception 异常
     */
    private static java.security.PrivateKey generatePrivateKey(byte[] keyBytes) throws Exception {
        try {
            // 尝试解析PKCS#8格式
            java.security.spec.KeySpec keySpec = new java.security.spec.PKCS8EncodedKeySpec(keyBytes);
            java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            // 尝试解析PKCS#1格式
            try {
                // 转换PKCS#1到PKCS#8
                byte[] pkcs8Bytes = convertPkcs1ToPkcs8(keyBytes);
                java.security.spec.KeySpec keySpec = new java.security.spec.PKCS8EncodedKeySpec(pkcs8Bytes);
                java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("RSA");
                return keyFactory.generatePrivate(keySpec);
            } catch (Exception ex) {
                throw new Exception("Failed to generate private key: " + ex.getMessage(), ex);
            }
        }
    }

    /**
     * 将PKCS#1格式转换为PKCS#8格式
     * @param pkcs1Bytes PKCS#1格式的私钥
     * @return PKCS#8格式的私钥
     * @throws Exception 异常
     */
    private static byte[] convertPkcs1ToPkcs8(byte[] pkcs1Bytes) throws Exception {
        // PKCS#8格式的ASN.1结构
        // SEQUENCE {
        //   version Version,
        //   privateKeyAlgorithm PrivateKeyAlgorithmIdentifier,
        //   privateKey PrivateKey
        // }
        // 其中PrivateKeyAlgorithmIdentifier对于RSA是:
        // SEQUENCE {
        //   algorithm OBJECT IDENTIFIER 1.2.840.113549.1.1.1,
        //   parameters NULL
        // }

        // 创建DER编码器
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        java.io.DataOutputStream dos = new java.io.DataOutputStream(baos);

        // 写入版本
        dos.writeByte(0x02); // INTEGER
        dos.writeByte(0x01); // length
        dos.writeByte(0x00); // value

        // 写入算法标识符
        dos.writeByte(0x30); // SEQUENCE
        dos.writeByte(0x0d); // length
        dos.writeByte(0x06); // OBJECT IDENTIFIER
        dos.writeByte(0x09); // length
        // 1.2.840.113549.1.1.1 (RSA)
        byte[] oid = {0x2a, (byte) 0x86, 0x48, (byte) 0x86, (byte) 0xf7, 0x0d, 0x01, 0x01, 0x01};
        dos.write(oid);
        dos.writeByte(0x05); // NULL
        dos.writeByte(0x00); // length

        // 写入私钥
        dos.writeByte(0x04); // OCTET STRING
        dos.writeByte(pkcs1Bytes.length); // length
        dos.write(pkcs1Bytes); // value

        // 写入整个结构
        java.io.ByteArrayOutputStream resultBaos = new java.io.ByteArrayOutputStream();
        java.io.DataOutputStream resultDos = new java.io.DataOutputStream(resultBaos);
        resultDos.writeByte(0x30); // SEQUENCE
        resultDos.writeByte((byte) baos.size()); // length
        resultDos.write(baos.toByteArray()); // value

        return resultBaos.toByteArray();
    }

    /**
     * 加载CA证书
     * @param caFile CA证书文件路径
     * @return CA证书列表
     * @throws Exception 异常
     */
    private static List<X509Certificate> loadCaCerts(String caFile) throws Exception {
        List<X509Certificate> caCerts = new ArrayList<>();
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        // 加载CA证书文件
        try (java.io.FileInputStream fis = new java.io.FileInputStream(caFile)) {
            caCerts.add((X509Certificate) cf.generateCertificate(fis));
        }

        return caCerts;
    }

    /**
     * 创建信任管理器工厂
     * @param caCerts CA证书列表
     * @return 信任管理器工厂
     * @throws Exception 异常
     */
    private static TrustManagerFactory createTrustManagerFactory(List<X509Certificate> caCerts) throws Exception {
        // 创建一个临时的密钥库，用于存储CA证书
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);

        // 将CA证书添加到密钥库
        for (int i = 0; i < caCerts.size(); i++) {
            X509Certificate cert = caCerts.get(i);
            trustStore.setCertificateEntry("ca" + i, cert);
        }

        // 创建信任管理器工厂
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm()
        );
        trustManagerFactory.init(trustStore);

        return trustManagerFactory;
    }
}