package com.xiaoniucode.etp.core.tls;

import com.xiaoniucode.etp.core.domain.TlsConfig;
import io.netty.handler.ssl.*;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;


public class TlsHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(TlsHelper.class);

    public static SslContext buildSslContext(boolean forClient, TlsConfig tlsConfig) throws IOException, CertificateException {
        SslProvider provider;
        if (OpenSsl.isAvailable()) {
            provider = SslProvider.OPENSSL;
            LOGGER.info("Using OpenSSL provider");
        } else {
            provider = SslProvider.JDK;
            LOGGER.info("Using JDK SSL provider");
        }
        boolean isTestMode = tlsConfig.isTestMode();
        if (forClient) {
            if (isTestMode) {
                return SslContextBuilder
                        .forClient()
                        .protocols("TLSv1.3")
                        .sslProvider(SslProvider.JDK)
                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                        .build();
            } else {
                SslContextBuilder sslContextBuilder = SslContextBuilder.forClient().sslProvider(SslProvider.JDK);

                if (!tlsConfig.mTLSEnabled()) {
                    sslContextBuilder.trustManager(InsecureTrustManagerFactory.INSTANCE);
                } else {
                    if (!isNullOrEmpty(tlsConfig.getCaFile())) {
                        sslContextBuilder.trustManager(new File(tlsConfig.getCaFile()));
                    }
                }

                return sslContextBuilder.keyManager(
                        !isNullOrEmpty(tlsConfig.getCertFile()) ? new FileInputStream(tlsConfig.getCertFile()) : null,
                        !isNullOrEmpty(tlsConfig.getKeyFile()) ? new FileInputStream(tlsConfig.getKeyFile()) : null
                ).build();
            }
        } else {
            if (isTestMode) {
                SelfSignedCertificate selfSignedCertificate = new SelfSignedCertificate();
                return SslContextBuilder
                        .forServer(selfSignedCertificate.certificate(), selfSignedCertificate.privateKey())
                        .sslProvider(provider)
                        .protocols("TLSv1.3")
                        .clientAuth(ClientAuth.OPTIONAL)
                        .build();
            } else {
                SslContextBuilder sslContextBuilder = SslContextBuilder.forServer(
                                !isNullOrEmpty(tlsConfig.getCertFile()) ? new FileInputStream(tlsConfig.getCertFile()) : null,
                                !isNullOrEmpty(tlsConfig.getKeyFile()) ? new FileInputStream(tlsConfig.getKeyFile()) : null
                        )
                        .sslProvider(provider);

                if (!tlsConfig.mTLSEnabled()) {
                    sslContextBuilder.trustManager(InsecureTrustManagerFactory.INSTANCE);
                } else {
                    if (!isNullOrEmpty(tlsConfig.getCaFile())) {
                        sslContextBuilder.trustManager(new File(tlsConfig.getCaFile()));
                    }
                }

                sslContextBuilder.clientAuth(tlsConfig.getClientAuthMode());
                return sslContextBuilder.build();
            }
        }
    }

    private static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }
}
