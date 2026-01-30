package com.xiaoniucode.etp.server.config;

import com.xiaoniucode.etp.common.config.Config;
import com.xiaoniucode.etp.common.log.LogConfig;
import com.xiaoniucode.etp.server.config.domain.Dashboard;
import com.xiaoniucode.etp.server.config.domain.KeystoreConfig;
import com.xiaoniucode.etp.server.config.domain.PortRange;
import com.xiaoniucode.etp.server.config.domain.ClientInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
@Setter
public class AppConfig implements Config {
    private String host;
    private int bindPort;
    private int httpProxyPort;
    private int httpsProxyPort;
    private LogConfig logConfig;
    private Dashboard dashboard;
    private PortRange portRange;
    private Set<String> baseDomains;

    private AppConfig(Builder builder) {
        this.host = builder.host;
        this.bindPort = builder.bindPort;
        this.httpProxyPort = builder.httpProxyPort;
        this.httpsProxyPort = builder.httpsProxyPort;
        this.logConfig = builder.logConfig;
        this.dashboard = builder.dashboard;
        this.portRange = builder.portRange;
        this.baseDomains = builder.baseDomains;
    }


    public static class Builder {
        private String host = "0.0.0.0";
        private int bindPort = 9527;
        private int httpProxyPort = 80;
        private int httpsProxyPort = 443;
        private boolean tls = false;
        private Set<String> baseDomains;
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
        }

        public Builder httpProxyPort(int httpProxyPort) {
            this.httpProxyPort = httpProxyPort;
            return this;
        }

        public Builder httpsProxyPort(int httpsProxyPort) {
            this.httpsProxyPort = httpsProxyPort;
            return this;
        }

        public Builder baseDomains(Set<String> baseDomains) {
            this.baseDomains = baseDomains;
            return this;
        }

        public AppConfig build() {
            return new AppConfig(this);
        }
    }


    public static Builder builder() {
        return new Builder();
    }
}
