package cn.xilio.etp.client;

import cn.xilio.etp.core.AbstractTlsContextFactory;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.TrustManagerFactory;
import java.io.File;

/**
 * 客户端SSL上下文工厂
 * @author liuxin
 */
public class ClientTlsContextFactory extends AbstractTlsContextFactory {
    private static final String TRUSTSTORE_PATH = System.getProperty("client.truststore.path");
    private static final String TRUSTSTORE_STORE_PASS = System.getProperty("client.truststore.storePass");
    private static final File CLIENT_TRUSTSTORE = new File(TRUSTSTORE_PATH);

    @Override
    public SslContext createContext() throws Exception {
        TrustManagerFactory clientTmf = loadTrustStore(CLIENT_TRUSTSTORE, TRUSTSTORE_STORE_PASS);
        return SslContextBuilder
                .forClient()
                .sslProvider(DEFAULT_SSL_PROVIDER)
                .protocols(PROTOCOL_TLS_1_3)
                .trustManager(clientTmf)
                .build();
    }
}
