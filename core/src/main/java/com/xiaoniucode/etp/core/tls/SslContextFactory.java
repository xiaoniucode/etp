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


public class SslContextFactory {

    public static SslContext createServerSslContext(TlsConfig config) throws Exception {
        KeyManagerFactory keyManagerFactory = createKeyManagerFactory(
                config.getCertFile(),
                config.getKeyFile()
        );

        SslContextBuilder builder = SslContextBuilder.forServer(keyManagerFactory);

        builder.protocols("TLSv1.3");

        if (config.isMtlsEnabled()) {
            List<X509Certificate> caCerts = loadCaCerts(config.getCaFile());
            TrustManagerFactory trustManagerFactory = createTrustManagerFactory(caCerts);
            builder.trustManager(trustManagerFactory)
                    .clientAuth(config.getClientAuthMode());
        } else {
            builder.clientAuth(config.getClientAuthMode());
        }

        return builder.build();
    }

    public static SslContext createClientSslContext(TlsConfig config) throws Exception {
        SslContextBuilder builder = SslContextBuilder.forClient();

        builder.protocols("TLSv1.3");

        if (config.isMtlsEnabled()) {
            List<X509Certificate> caCerts = loadCaCerts(config.getCaFile());
            TrustManagerFactory trustManagerFactory = createTrustManagerFactory(caCerts);
            builder.trustManager(trustManagerFactory);
        } else {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm()
            );
            trustManagerFactory.init((KeyStore) null);
            builder.trustManager(trustManagerFactory);
        }

        if (config.getCertFile() != null && !config.getCertFile().isEmpty() &&
                config.getKeyFile() != null && !config.getKeyFile().isEmpty()) {
            KeyManagerFactory keyManagerFactory = createKeyManagerFactory(
                    config.getCertFile(),
                    config.getKeyFile()
            );
            builder.keyManager(keyManagerFactory);
        }

        return builder.build();
    }

    private static KeyManagerFactory createKeyManagerFactory(String certFile, String keyFile) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        try (FileInputStream fis = new FileInputStream(certFile)) {
            X509Certificate cert = (X509Certificate) cf.generateCertificate(fis);
            keyStore.setCertificateEntry("cert", cert);
        }

        byte[] keyBytes = readPrivateKey(keyFile);
        java.security.PrivateKey privateKey = generatePrivateKey(keyBytes);

        keyStore.setKeyEntry("key", privateKey, "".toCharArray(), new java.security.cert.Certificate[]{
                keyStore.getCertificate("cert")
        });

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm()
        );
        keyManagerFactory.init(keyStore, "".toCharArray());

        return keyManagerFactory;
    }

    private static byte[] readPrivateKey(String keyFile) throws Exception {
        try (FileInputStream fis = new FileInputStream(keyFile)) {
            byte[] allBytes = fis.readAllBytes();
            String keyContent = new String(allBytes);

            keyContent = keyContent.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                    .replace("-----END RSA PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            return Base64.getDecoder().decode(keyContent);
        }
    }

    private static java.security.PrivateKey generatePrivateKey(byte[] keyBytes) throws Exception {
        try {
            java.security.spec.KeySpec keySpec = new java.security.spec.PKCS8EncodedKeySpec(keyBytes);
            java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            try {
                byte[] pkcs8Bytes = convertPkcs1ToPkcs8(keyBytes);
                java.security.spec.KeySpec keySpec = new java.security.spec.PKCS8EncodedKeySpec(pkcs8Bytes);
                java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("RSA");
                return keyFactory.generatePrivate(keySpec);
            } catch (Exception ex) {
                throw new Exception("Failed to generate private key: " + ex.getMessage(), ex);
            }
        }
    }

    private static byte[] convertPkcs1ToPkcs8(byte[] pkcs1Bytes) throws Exception {
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

    private static List<X509Certificate> loadCaCerts(String caFile) throws Exception {
        List<X509Certificate> caCerts = new ArrayList<>();
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        try (java.io.FileInputStream fis = new java.io.FileInputStream(caFile)) {
            caCerts.add((X509Certificate) cf.generateCertificate(fis));
        }

        return caCerts;
    }

    private static TrustManagerFactory createTrustManagerFactory(List<X509Certificate> caCerts) throws Exception {
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);

        for (int i = 0; i < caCerts.size(); i++) {
            X509Certificate cert = caCerts.get(i);
            trustStore.setCertificateEntry("ca" + i, cert);
        }

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm()
        );
        trustManagerFactory.init(trustStore);

        return trustManagerFactory;
    }
}