package cn.xilio.etp.client;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

public final class ClientSslContextFactory {

    /**
     * 方式1：信任系统根证书（正规证书用这条）
     */
    public static SslContext buildDefault() throws Exception {
        return SslContextBuilder.forClient().build();
    }

    /**
     * 方式2：信任「resources/ssl/tunnel.jks」里的自签证书
     */
    public static SslContext buildWithJks() {
        try {
            try (InputStream in = ClientSslContextFactory.class
                    .getClassLoader().getResourceAsStream("ssl/tunnel.jks")) {
                KeyStore ts = KeyStore.getInstance("JKS");
                ts.load(in, "123456".toCharArray());

                TrustManagerFactory tmf =
                        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(ts);

                return SslContextBuilder.forClient()
                        .trustManager(tmf)
                        .build();
            }
        } catch (Exception e) {
            //todo test
            return null;
        }
    }

    private ClientSslContextFactory() {
    }
}
