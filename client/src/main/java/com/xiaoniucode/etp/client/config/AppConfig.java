package com.xiaoniucode.etp.client.config;

import com.xiaoniucode.etp.client.config.domain.AuthConfig;
import com.xiaoniucode.etp.client.config.domain.ConnectionPoolConfig;
import com.xiaoniucode.etp.client.config.domain.LogConfig;
import com.xiaoniucode.etp.client.config.domain.MultiplexConfig;
import com.xiaoniucode.etp.common.config.Config;
import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.domain.TlsConfig;
import com.xiaoniucode.etp.core.enums.AgentType;

import java.util.List;

public interface AppConfig extends Config {
    String getServerAddr();

    int getServerPort();

    AuthConfig getAuthConfig();
    MultiplexConfig getMultiplexConfig();
    ConnectionPoolConfig getConnectionPoolConfig();

    TlsConfig getTlsConfig();

    List<ProxyConfig> getProxies();

    LogConfig getLogConfig();
    AgentType getAgentType();

}
