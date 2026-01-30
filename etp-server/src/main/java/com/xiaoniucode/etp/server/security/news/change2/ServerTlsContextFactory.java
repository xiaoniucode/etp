package com.xiaoniucode.etp.server.security.news.change2;

import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSL上下文创建工厂
 *
 * @author liuxin
 */
public class ServerTlsContextFactory extends AbstractTlsContextFactory {

    private final Map<String, SslContext> contextCache = new ConcurrentHashMap<>();

    /**
     * 创建默认SSL上下文
     */
    @Override
    public SslContext createContext() throws Exception {
        throw new UnsupportedOperationException("Default createContext not supported, use createContext with parameters");
    }

    /**
     * 创建单向认证SSL上下文
     * @param keyStorePath 密钥库文件路径
     * @param keyPass 私钥密码
     * @param storePass 存储库密码
     * @return SSL上下文
     */
    public SslContext createOneWayAuthContext(String keyStorePath, String keyPass, String storePass) throws Exception {
        return createOneWayAuthContext(keyStorePath, keyPass, storePass, DEFAULT_KEYSTORE_TYPE, DEFAULT_PROTOCOL_TLS);
    }

    /**
     * 创建单向认证SSL上下文
     * @param keyStorePath 密钥库文件路径
     * @param keyPass 私钥密码
     * @param storePass 存储库密码
     * @param keyStoreType 密钥库类型
     * @param protocol TLS协议版本
     * @return SSL上下文
     */
    public SslContext createOneWayAuthContext(String keyStorePath, String keyPass, String storePass, String keyStoreType, String protocol) throws Exception {
        String key = keyStorePath + ":" + keyPass + ":" + storePass + ":" + keyStoreType + ":" + protocol + ":one-way";
        return contextCache.computeIfAbsent(key, k -> {
            try {
                File serverKeystore = new File(keyStorePath);
                KeyManagerFactory serverKmf = loadKeyStore(serverKeystore, keyPass, storePass, keyStoreType);
                return SslContextBuilder
                        .forServer(serverKmf)
                        .sslProvider(DEFAULT_SSL_PROVIDER)
                        .protocols(protocol)
                        .clientAuth(ClientAuth.NONE)
                        .build();
            } catch (Exception e) {
                throw new RuntimeException("Failed to create one-way auth SSL context", e);
            }
        });
    }

    /**
     * 创建双向认证SSL上下文
     * @param keyStorePath 密钥库文件路径
     * @param keyPass 私钥密码
     * @param storePass 存储库密码
     * @param trustStorePath 信任库文件路径
     * @param trustStorePass 信任库密码
     * @return SSL上下文
     */
    public SslContext createTwoWayAuthContext(String keyStorePath, String keyPass, String storePass, String trustStorePath, String trustStorePass) throws Exception {
        return createTwoWayAuthContext(keyStorePath, keyPass, storePass, trustStorePath, trustStorePass, DEFAULT_KEYSTORE_TYPE, DEFAULT_KEYSTORE_TYPE, DEFAULT_PROTOCOL_TLS);
    }

    /**
     * 创建双向认证SSL上下文
     * @param keyStorePath 密钥库文件路径
     * @param keyPass 私钥密码
     * @param storePass 存储库密码
     * @param trustStorePath 信任库文件路径
     * @param trustStorePass 信任库密码
     * @param keyStoreType 密钥库类型
     * @param trustStoreType 信任库类型
     * @param protocol TLS协议版本
     * @return SSL上下文
     */
    public SslContext createTwoWayAuthContext(String keyStorePath, String keyPass, String storePass, String trustStorePath, String trustStorePass, String keyStoreType, String trustStoreType, String protocol) throws Exception {
        String key = keyStorePath + ":" + keyPass + ":" + storePass + ":" + trustStorePath + ":" + trustStorePass + ":" + keyStoreType + ":" + trustStoreType + ":" + protocol + ":two-way";
        return contextCache.computeIfAbsent(key, k -> {
            try {
                File serverKeystore = new File(keyStorePath);
                File trustStoreFile = new File(trustStorePath);
                
                KeyManagerFactory serverKmf = loadKeyStore(serverKeystore, keyPass, storePass, keyStoreType);
                TrustManagerFactory serverTmf = loadTrustStore(trustStoreFile, trustStorePass, trustStoreType);
                
                return SslContextBuilder
                        .forServer(serverKmf)
                        .sslProvider(DEFAULT_SSL_PROVIDER)
                        .protocols(protocol)
                        .trustManager(serverTmf)
                        .clientAuth(ClientAuth.REQUIRE)
                        .build();
            } catch (Exception e) {
                throw new RuntimeException("Failed to create two-way auth SSL context", e);
            }
        });
    }

    /**
     * 清除上下文缓存
     */
    public void clearCache() {
        contextCache.clear();
    }

    /**
     * 获取缓存中的上下文数量
     */
    public int getCacheSize() {
        return contextCache.size();
    }
}
