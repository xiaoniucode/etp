package com.xiaoniucode.etp.server.security.news.change2;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客户端SSL上下文工厂
 *
 * @author liuxin
 */
public class ClientTlsContextFactory extends AbstractTlsContextFactory {

    private final Map<String, SslContext> contextCache = new ConcurrentHashMap<>();

    /**
     * 创建默认SSL上下文
     */
    @Override
    public SslContext createContext() throws Exception {
        throw new UnsupportedOperationException("Default createContext not supported, use createContext with parameters");
    }

    /**
     * 创建指定配置的SSL上下文
     * @param trustStorePath 信任库文件路径
     * @param trustStorePass 信任库密码
     * @return SSL上下文
     */
    public SslContext createContext(String trustStorePath, String trustStorePass) throws Exception {
        return createContext(trustStorePath, trustStorePass, DEFAULT_KEYSTORE_TYPE, DEFAULT_PROTOCOL_TLS);
    }

    /**
     * 创建指定配置的SSL上下文
     * @param trustStorePath 信任库文件路径
     * @param trustStorePass 信任库密码
     * @param trustStoreType 信任库类型
     * @param protocol TLS协议版本
     * @return SSL上下文
     */
    public SslContext createContext(String trustStorePath, String trustStorePass, String trustStoreType, String protocol) throws Exception {
        String key = trustStorePath + ":" + trustStorePass + ":" + trustStoreType + ":" + protocol;
        return contextCache.computeIfAbsent(key, k -> {
            try {
                File trustStoreFile = new File(trustStorePath);
                TrustManagerFactory clientTmf = loadTrustStore(trustStoreFile, trustStorePass, trustStoreType);
                return SslContextBuilder
                        .forClient()
                        .sslProvider(DEFAULT_SSL_PROVIDER)
                        .protocols(protocol)
                        .trustManager(clientTmf)
                        .build();
            } catch (Exception e) {
                throw new RuntimeException("Failed to create SSL context", e);
            }
        });
    }

    /**
     * 创建支持双向认证的SSL上下文
     * @param keyStorePath 密钥库文件路径
     * @param keyStorePass 密钥库密码
     * @param keyPass 私钥密码
     * @param trustStorePath 信任库文件路径
     * @param trustStorePass 信任库密码
     * @return SSL上下文
     */
    public SslContext createMutualAuthContext(String keyStorePath, String keyStorePass, String keyPass, String trustStorePath, String trustStorePass) throws Exception {
        return createMutualAuthContext(keyStorePath, keyStorePass, keyPass, trustStorePath, trustStorePass, DEFAULT_KEYSTORE_TYPE, DEFAULT_KEYSTORE_TYPE, DEFAULT_PROTOCOL_TLS);
    }

    /**
     * 创建支持双向认证的SSL上下文
     * @param keyStorePath 密钥库文件路径
     * @param keyStorePass 密钥库密码
     * @param keyPass 私钥密码
     * @param trustStorePath 信任库文件路径
     * @param trustStorePass 信任库密码
     * @param keyStoreType 密钥库类型
     * @param trustStoreType 信任库类型
     * @param protocol TLS协议版本
     * @return SSL上下文
     */
    public SslContext createMutualAuthContext(String keyStorePath, String keyStorePass, String keyPass, String trustStorePath, String trustStorePass, String keyStoreType, String trustStoreType, String protocol) throws Exception {
        String key = keyStorePath + ":" + keyStorePass + ":" + keyPass + ":" + trustStorePath + ":" + trustStorePass + ":" + keyStoreType + ":" + trustStoreType + ":" + protocol;
        return contextCache.computeIfAbsent(key, k -> {
            try {
                File keyStoreFile = new File(keyStorePath);
                File trustStoreFile = new File(trustStorePath);
                
                TrustManagerFactory clientTmf = loadTrustStore(trustStoreFile, trustStorePass, trustStoreType);
                return SslContextBuilder
                        .forClient()
                        .sslProvider(DEFAULT_SSL_PROVIDER)
                        .protocols(protocol)
                        .keyManager(loadKeyStore(keyStoreFile, keyPass, keyStorePass, keyStoreType))
                        .trustManager(clientTmf)
                        .build();
            } catch (Exception e) {
                throw new RuntimeException("Failed to create mutual auth SSL context", e);
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
