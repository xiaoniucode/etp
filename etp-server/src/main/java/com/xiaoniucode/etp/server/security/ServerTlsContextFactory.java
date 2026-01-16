package com.xiaoniucode.etp.server.security;

import com.xiaoniucode.etp.core.AbstractTlsContextFactory;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.config.ConfigHelper;
import com.xiaoniucode.etp.server.config.domain.KeystoreConfig;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.KeyManagerFactory;
import java.io.File;

/**
 * SSL上下文创建工厂
 *
 * @author liuxin
 */
public class ServerTlsContextFactory extends AbstractTlsContextFactory {
    @Override
    public SslContext createContext() throws Exception {
        AppConfig config = ConfigHelper.get();
        KeystoreConfig keystore = config.getKeystoreConfig();

        String path = keystore.getPath();
        String keyPass = keystore.getKeyPass();
        String storePass = keystore.getStorePass();
        File serverKeystore = new File(path);

        KeyManagerFactory serverKmf = loadKeyStore(serverKeystore, keyPass, storePass);
        return SslContextBuilder
                .forServer(serverKmf)
                .sslProvider(DEFAULT_SSL_PROVIDER)
                .protocols(PROTOCOL_TLS_1_3)
                .clientAuth(ClientAuth.NONE)
                .build();
    }
}
