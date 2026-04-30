package com.xiaoniucode.etp.core.transport.tls;

import com.xiaoniucode.etp.common.utils.StringUtils;
import com.xiaoniucode.etp.core.domain.TlsConfig;
import io.netty.handler.ssl.*;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;

public class TlsHelper {
    private static final String DEFAULT_PROTOCOL = "TLSv1.3";

    public static SslContext buildSslContext(boolean forClient, TlsConfig tlsConfig, boolean isTestMode) throws IOException, CertificateException {
        SslProvider provider = SslProvider.JDK;
        if (forClient) {
            return buildClientSslContext(tlsConfig, isTestMode, provider);
        } else {
            return buildServerSslContext(tlsConfig, isTestMode, provider);
        }
    }

    private static SslContext buildClientSslContext(TlsConfig tlsConfig, boolean isTestMode, SslProvider provider) throws IOException {
        if (isTestMode) {
            return createTestSslContextForClient(provider);
        } else {
            return createSslContextForClient(tlsConfig, provider);
        }
    }


    private static SslContext buildServerSslContext(TlsConfig tlsConfig, boolean isTestMode, SslProvider provider) throws IOException, CertificateException {
        if (isTestMode) {
            return createTestSslContextForServer(provider);
        } else {
            return createSslContextForServer(tlsConfig, provider);
        }
    }

    private static SslContext createTestSslContextForClient(SslProvider provider) throws IOException {
        return SslContextBuilder
                .forClient()
                .protocols(DEFAULT_PROTOCOL)
                .sslProvider(provider)
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();
    }


    private static SslContext createSslContextForClient(TlsConfig tlsConfig, SslProvider provider) throws IOException {
        SslContextBuilder sslContextBuilder = SslContextBuilder
                .forClient()
                .protocols(DEFAULT_PROTOCOL)
                .sslProvider(provider);

        if (!tlsConfig.mTLSEnabled()) {
            sslContextBuilder.trustManager(InsecureTrustManagerFactory.INSTANCE);
        } else {
            if (StringUtils.hasText(tlsConfig.getCaFile())) {
                sslContextBuilder.trustManager(new File(tlsConfig.getCaFile()));
            }
        }

        return sslContextBuilder.keyManager(
                StringUtils.hasText(tlsConfig.getCertFile()) ? new FileInputStream(tlsConfig.getCertFile()) : null,
                StringUtils.hasText(tlsConfig.getKeyFile()) ? new FileInputStream(tlsConfig.getKeyFile()) : null
        ).build();
    }


    private static SslContext createTestSslContextForServer(SslProvider provider) throws IOException, CertificateException {
        SelfSignedCertificate selfSignedCertificate = new SelfSignedCertificate();
        return SslContextBuilder
                .forServer(selfSignedCertificate.certificate(), selfSignedCertificate.privateKey())
                .sslProvider(provider)
                .protocols(DEFAULT_PROTOCOL)
                .clientAuth(ClientAuth.OPTIONAL)
                .build();
    }

    private static SslContext createSslContextForServer(TlsConfig tlsConfig, SslProvider provider) throws IOException {
        SslContextBuilder sslContextBuilder = SslContextBuilder
                .forServer(
                        StringUtils.hasText(tlsConfig.getCertFile()) ? new FileInputStream(tlsConfig.getCertFile()) : null,
                        StringUtils.hasText(tlsConfig.getKeyFile()) ? new FileInputStream(tlsConfig.getKeyFile()) : null
                )
                .protocols(DEFAULT_PROTOCOL)
                .sslProvider(provider);

        if (!tlsConfig.mTLSEnabled()) {
            sslContextBuilder.trustManager(InsecureTrustManagerFactory.INSTANCE);
        } else {
            if (StringUtils.hasText(tlsConfig.getCaFile())) {
                sslContextBuilder.trustManager(new File(tlsConfig.getCaFile()));
            }
        }
        sslContextBuilder.clientAuth(tlsConfig.getClientAuthMode());
        return sslContextBuilder.build();
    }
}