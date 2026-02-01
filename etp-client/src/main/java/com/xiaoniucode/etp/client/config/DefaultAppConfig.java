package com.xiaoniucode.etp.client.config;

import com.xiaoniucode.etp.client.config.domain.AuthConfig;
import com.xiaoniucode.etp.client.config.domain.TlsConfig;
import com.xiaoniucode.etp.client.config.domain.LogConfig;
import com.xiaoniucode.etp.core.domain.ProxyConfig;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DefaultAppConfig implements AppConfig {
    private final String serverAddr;
    private final int serverPort;
    private final AuthConfig authConfig;
    private final TlsConfig tlsConfig;
    private final List<ProxyConfig> proxies;
    private final LogConfig logConfig;

    private DefaultAppConfig(Builder builder) {
        this.serverAddr = builder.serverAddr;
        this.serverPort = builder.serverPort;
        this.authConfig = builder.authConfig;
        this.tlsConfig = builder.tlsConfig;
        this.proxies = builder.proxies;
        this.logConfig = builder.logConfig;
    }

    public static class Builder {
        private String serverAddr = "127.0.0.1";
        private int serverPort = 9527;
        private AuthConfig authConfig;
        private TlsConfig tlsConfig = new TlsConfig();
        private List<ProxyConfig> proxies = new CopyOnWriteArrayList<>();
        private LogConfig logConfig;

        public Builder serverAddr(String serverAddr) {
            this.serverAddr = serverAddr;
            return this;
        }

        public Builder serverPort(int serverPort) {
            this.serverPort = serverPort;
            return this;
        }

        public Builder authConfig(AuthConfig authConfig) {
            this.authConfig = authConfig;
            return this;
        }

        public Builder tlsConfig(TlsConfig tlsConfig) {
            this.tlsConfig = tlsConfig;
            return this;
        }

        public Builder proxies(List<ProxyConfig> proxies) {
            this.proxies = proxies;
            return this;
        }

        public Builder logConfig(LogConfig logConfig) {
            this.logConfig = logConfig;
            return this;
        }

        public AppConfig build() {
            return new DefaultAppConfig(this);
        }
    }

    @Override
    public String getServerAddr() {
        return serverAddr;
    }

    @Override
    public int getServerPort() {
        return serverPort;
    }

    @Override
    public AuthConfig getAuthConfig() {
        return authConfig;
    }

    @Override
    public TlsConfig getTlsConfig() {
        return tlsConfig;
    }

    @Override
    public List<ProxyConfig> getProxies() {
        return proxies;
    }

    @Override
    public LogConfig getLogConfig() {
        return logConfig;
    }

    public static Builder builder() {
        return new Builder();
    }
}
