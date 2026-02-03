package com.xiaoniucode.etp.server.config;

import com.xiaoniucode.etp.common.config.Config;
import com.xiaoniucode.etp.common.log.LogConfig;
import com.xiaoniucode.etp.server.config.domain.Dashboard;
import com.xiaoniucode.etp.server.config.domain.PortRange;
import com.xiaoniucode.etp.server.config.domain.AccessTokenInfo;
import com.xiaoniucode.etp.server.config.domain.TLSConfig;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
@Setter
public class AppConfig implements Config {
    private String serverAddr;
    private int serverPort;
    private int httpProxyPort;
    private int httpsProxyPort;
    private LogConfig logConfig;
    private Dashboard dashboard;
    private PortRange portRange;
    private Set<String> baseDomains;
    private TLSConfig tls;
    private List<AccessTokenInfo> accessTokens;

    private AppConfig(Builder builder) {
        this.serverAddr = builder.serverAddr;
        this.serverPort = builder.serverPort;
        this.httpProxyPort = builder.httpProxyPort;
        this.httpsProxyPort = builder.httpsProxyPort;
        this.logConfig = builder.logConfig;
        this.dashboard = builder.dashboard;
        this.portRange = builder.portRange;
        this.baseDomains = builder.baseDomains;
        this.tls = builder.tls;
        this.accessTokens = builder.accessTokens;
    }

    public static class Builder {
        private String serverAddr = "0.0.0.0";
        private int serverPort = 9527;
        private int httpProxyPort = 80;
        private int httpsProxyPort = 443;
        private TLSConfig tls;
        private Set<String> baseDomains;
        private LogConfig logConfig;
        private Dashboard dashboard = new Dashboard(false);
        private PortRange portRange = new PortRange(1, 65535);
        private List<AccessTokenInfo> accessTokens = new CopyOnWriteArrayList<>();

        public Builder serverAddr(String serverAddr) {
            this.serverAddr = serverAddr;
            return this;
        }

        public Builder serverPort(int serverPort) {
            this.serverPort = serverPort;
            return this;
        }

        public Builder tls(TLSConfig tls) {
            this.tls = tls;
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

        public Builder accessTokens(List<AccessTokenInfo> accessTokens) {
            this.accessTokens = accessTokens;
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
