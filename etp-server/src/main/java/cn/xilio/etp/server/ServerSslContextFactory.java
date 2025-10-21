package cn.xilio.etp.server;

import cn.xilio.etp.core.AbstractSslContextFactory;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.KeyManagerFactory;
import java.io.File;

/**
 * 服务端SSL上下文工厂（单向认证）
 * @author xilio.cn
 */
public class ServerSslContextFactory extends AbstractSslContextFactory {
    private static final String KEYSTORE_PATH = System.getProperty("server.keystore.path");
    private static final String KEYSTORE_PASS = System.getProperty("server.keystore.pass");
    private static final File SERVER_KEYSTORE = new File(KEYSTORE_PATH);

    @Override
    public SslContext createContext() throws Exception {
        // 调用父类方法加载密钥库
        KeyManagerFactory serverKmf = loadKeyStore(SERVER_KEYSTORE, KEYSTORE_PASS);

        return SslContextBuilder
                .forServer(serverKmf)
                .sslProvider(DEFAULT_SSL_PROVIDER)
                .protocols(PROTOCOL_TLS_1_3)
                .clientAuth(ClientAuth.NONE)
                .build();
    }
}
