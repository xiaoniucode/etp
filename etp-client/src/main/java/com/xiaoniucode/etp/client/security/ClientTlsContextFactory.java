package com.xiaoniucode.etp.client.security;

import com.xiaoniucode.etp.client.config.AppConfig;
import com.xiaoniucode.etp.client.config.ConfigHelper;
import com.xiaoniucode.etp.client.config.TruststoreConfig;
import com.xiaoniucode.etp.core.AbstractTlsContextFactory;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.TrustManagerFactory;
import java.io.File;

/**
 * 客户端SSL上下文工厂
 *
 * @author liuxin
 */
public class ClientTlsContextFactory extends AbstractTlsContextFactory {

    @Override
    public SslContext createContext() throws Exception {
        AppConfig config = ConfigHelper.get();
        TruststoreConfig truststore = config.getTruststore();

        String path = truststore.getPath();
        String storePass = truststore.getStorePass();
        File trustStoreFile = new File(path);
        TrustManagerFactory clientTmf = loadTrustStore(trustStoreFile, storePass);
        return SslContextBuilder
                .forClient()
                .sslProvider(DEFAULT_SSL_PROVIDER)
                .protocols(PROTOCOL_TLS_1_3)
                .trustManager(clientTmf)
                .build();
    }
}
