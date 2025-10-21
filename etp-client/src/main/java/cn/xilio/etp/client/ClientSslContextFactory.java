package cn.xilio.etp.client;

import cn.xilio.etp.core.AbstractSslContextFactory;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.TrustManagerFactory;
import java.io.File;

/**
 * 客户端SSL上下文工厂（单向认证）
 */
public class ClientSslContextFactory extends AbstractSslContextFactory {
    private static final String TRUSTSTORE_PATH = System.getProperty("client.truststore.path");
    private static final String TRUSTSTORE_PASS = System.getProperty("client.truststore.pass");
    private static final File CLIENT_TRUSTSTORE = new File(TRUSTSTORE_PATH);

    @Override
    public SslContext createContext() throws Exception {
        // 调用父类方法加载信任库
        TrustManagerFactory clientTmf = loadTrustStore(CLIENT_TRUSTSTORE, TRUSTSTORE_PASS);
        return SslContextBuilder
                .forClient()
                .sslProvider(DEFAULT_SSL_PROVIDER)
                .protocols(PROTOCOL_TLS_1_3)
                .trustManager(clientTmf)
                .build();
    }
}
