package cn.xilio.etp.server;

import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.KeyManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

public class SslContextFactory {

    private static final String KEYSTORE_FILE = "ssl/tunnel.jks";
    private static final String KEYSTORE_PASS = "123456";
    private static final String KEY_PASS = "123456";

    public static SslContext createServerSslContext() {
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            try (InputStream is = SslContextFactory.class.getClassLoader().getResourceAsStream(KEYSTORE_FILE)) {
                keyStore.load(is, KEYSTORE_PASS.toCharArray());
            }

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, KEY_PASS.toCharArray());

            return SslContextBuilder.forServer(kmf)
                    .clientAuth(ClientAuth.NONE)
                    .build();
        } catch (Exception e) {
            return null;
        }
    }
}
