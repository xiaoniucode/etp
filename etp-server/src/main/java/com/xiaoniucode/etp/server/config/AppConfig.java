package com.xiaoniucode.etp.server.config;

import com.xiaoniucode.etp.common.config.Config;
import com.xiaoniucode.etp.common.log.LogConfig;
import com.xiaoniucode.etp.server.config.domain.Dashboard;
import com.xiaoniucode.etp.server.config.domain.KeystoreConfig;
import com.xiaoniucode.etp.server.config.domain.PortRange;
import com.xiaoniucode.etp.server.config.domain.ClientInfo;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AppConfig implements Config {
    private String host;
    private int bindPort;
    private int httpProxyPort;
    private boolean tls;
    private KeystoreConfig keystoreConfig;
    private LogConfig logConfig;
    private Dashboard dashboard;
    private PortRange portRange;
    private List<ClientInfo> clients;

    private AppConfig(Builder builder) {
        this.host = builder.host;
        this.bindPort = builder.bindPort;
        this.httpProxyPort = builder.httpProxyPort;
        this.tls = builder.tls;
        this.keystoreConfig = builder.keystoreConfig;
        this.logConfig = builder.logConfig;
        this.dashboard = builder.dashboard;
        this.portRange = builder.portRange;
        this.clients = builder.clients;
    }

    public static class Builder {
        private String host = "0.0.0.0";
        private int bindPort = 9527;
        private int httpProxyPort = 80;
        private boolean tls = false;
        private KeystoreConfig keystoreConfig;
        private LogConfig logConfig;
        private Dashboard dashboard = new Dashboard(false);
        private PortRange portRange = new PortRange(1024, 49151);
        private List<ClientInfo> clients = new CopyOnWriteArrayList<>();

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder bindPort(int bindPort) {
            this.bindPort = bindPort;
            return this;
        }

        public Builder tls(boolean tls) {
            this.tls = tls;
            return this;
        }

        public Builder keystoreConfig(KeystoreConfig keystoreConfig) {
            this.keystoreConfig = keystoreConfig;
            return this;
        }

        public Builder logConfig(LogConfig logConfig) {
            this.logConfig = logConfig;
            return this;
        }

        public Builder dashboard(Dashboard dashboard) {
            this.dashboard = dashboard;
            return this;
        }

        public Builder portRange(PortRange portRange) {
            this.portRange = portRange;
            return this;
        }

        public Builder clients(List<ClientInfo> clients) {
            this.clients = clients;
            return this;
        }public Builder httpProxyPort(int httpProxyPort) {
            this.httpProxyPort = httpProxyPort;
            return this;
        }

        public AppConfig build() {
            return new AppConfig(this);
        }
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getBindPort() {
        return bindPort;
    }

    public void setBindPort(int bindPort) {
        this.bindPort = bindPort;
    }

    public boolean isTls() {
        return tls;
    }

    public void setTls(boolean tls) {
        this.tls = tls;
    }

    public KeystoreConfig getKeystoreConfig() {
        return keystoreConfig;
    }

    public void setKeystoreConfig(KeystoreConfig keystoreConfig) {
        this.keystoreConfig = keystoreConfig;
    }

    public LogConfig getLogConfig() {
        return logConfig;
    }

    public void setLogConfig(LogConfig logConfig) {
        this.logConfig = logConfig;
    }

    public Dashboard getDashboard() {
        return dashboard;
    }

    public void setDashboard(Dashboard dashboard) {
        this.dashboard = dashboard;
    }

    public PortRange getPortRange() {
        return portRange;
    }

    public void setPortRange(PortRange portRange) {
        this.portRange = portRange;
    }

    public List<ClientInfo> getClients() {
        return clients;
    }

    public void setClients(List<ClientInfo> clients) {
        this.clients = clients;
    }

    public void setHttpProxyPort(int httpProxyPort) {
        this.httpProxyPort = httpProxyPort;
    }

    public int getHttpProxyPort() {
        return httpProxyPort;
    }

    public static Builder builder() {
        return new Builder();
    }
}
