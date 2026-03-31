package com.xiaoniucode.etp.client.config;

import com.xiaoniucode.etp.client.config.domain.AuthConfig;
import com.xiaoniucode.etp.client.config.domain.ConnectionPoolConfig;
import com.xiaoniucode.etp.client.config.domain.LogConfig;
import com.xiaoniucode.etp.client.config.domain.MultiplexConfig;
import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.domain.TlsConfig;
import com.xiaoniucode.etp.core.enums.AgentType;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DefaultAppConfig implements AppConfig {
    private final String serverAddr;
    private final int serverPort;
    private final AuthConfig authConfig;
    private final TlsConfig tlsConfig;
    private final List<ProxyConfig> proxies;
    private final LogConfig logConfig;
    private final AgentType clientType;
    private final MultiplexConfig multiplexConfig;
    private final ConnectionPoolConfig connectionPoolConfig;

    private DefaultAppConfig(Builder builder) {
        this.serverAddr = builder.serverAddr;
        this.serverPort = builder.serverPort;
        this.authConfig = builder.authConfig;
        this.tlsConfig = builder.tlsConfig;
        this.proxies = builder.proxies;
        this.logConfig = builder.logConfig;
        this.clientType = builder.agentType;
        this.multiplexConfig = builder.muxConfig;
        this.connectionPoolConfig = builder.connectionPoolConfig;
    }

    public static class Builder {
        private String serverAddr = "127.0.0.1";
        private int serverPort = 9527;
        private AuthConfig authConfig = new AuthConfig();
        private TlsConfig tlsConfig = new TlsConfig(true, true);
        private List<ProxyConfig> proxies = new CopyOnWriteArrayList<>();
        private LogConfig logConfig;
        private AgentType agentType = AgentType.BINARY;
        private MultiplexConfig muxConfig = new MultiplexConfig(true);
        private ConnectionPoolConfig connectionPoolConfig = new ConnectionPoolConfig();

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

        public Builder muxConfig(MultiplexConfig muxConfig) {
            this.muxConfig = muxConfig;
            return this;
        }

        public Builder connectionPoolConfig(ConnectionPoolConfig connectionPoolConfig) {
            this.connectionPoolConfig = connectionPoolConfig;
            return this;
        }

        public Builder addProxies(List<ProxyConfig> proxies) {
            if (proxies != null) {
                this.proxies.addAll(proxies);
            }
            return this;
        }

        public Builder addProxy(ProxyConfig proxyConfig) {
            if (this.proxies != null) {
                this.proxies.add(proxyConfig);
            }
            return this;
        }

        public Builder logConfig(LogConfig logConfig) {
            this.logConfig = logConfig;
            return this;
        }

        public Builder agentType(AgentType agentType) {
            this.agentType = agentType;
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
    public MultiplexConfig getMultiplexConfig() {
        return multiplexConfig;
    }

    @Override
    public ConnectionPoolConfig getConnectionPoolConfig() {
        return connectionPoolConfig;
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

    @Override
    public AgentType getAgentType() {
        return clientType;
    }

    public static Builder builder() {
        return new Builder();
    }
}
