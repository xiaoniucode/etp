package com.xiaoniucode.etp.server;

import com.xiaoniucode.etp.core.AbstractTlsContextFactory;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.KeyManagerFactory;
import java.io.File;

/**
 * 服务端SSL上下文工厂
 *
 * @author liuxin
 */
public class ServerTlsContextFactory extends AbstractTlsContextFactory {
    private static final String KEYSTORE_PATH = System.getProperty("server.keystore.path");
    private static final String KEYSTORE_KEY_PASS = System.getProperty("server.keystore.keyPass");
    private static final String KEYSTORE_STORE_PASS = System.getProperty("server.keystore.storePass");
    private static final File SERVER_KEYSTORE = new File(KEYSTORE_PATH);

    @Override
    public SslContext createContext() throws Exception {
        KeyManagerFactory serverKmf = loadKeyStore(SERVER_KEYSTORE, KEYSTORE_KEY_PASS, KEYSTORE_STORE_PASS);
        return SslContextBuilder
                .forServer(serverKmf)
                .sslProvider(DEFAULT_SSL_PROVIDER)
                .protocols(PROTOCOL_TLS_1_3)
                .clientAuth(ClientAuth.NONE)
                .build();
    }
}
