package com.xiaoniucode.etp.server.config;

import com.xiaoniucode.etp.common.config.ConfigSource;
import com.xiaoniucode.etp.common.config.ConfigSourceType;
import com.xiaoniucode.etp.common.log.LogConfig;
import com.xiaoniucode.etp.common.utils.LogUtils;
import com.xiaoniucode.etp.common.utils.StringUtils;
import com.xiaoniucode.etp.common.utils.TomlUtils;
import com.moandjiezana.toml.Toml;
import com.xiaoniucode.etp.server.config.domain.*;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author xiaoniucode
 */
public class TomlConfigSource implements ConfigSource {
    private final String path;
    private static final String DEFAULT_HOST = "0.0.0.0";
    private static final int DEFAULT_BIND_PORT = 9527;
    private static final String DEFAULT_DASHBOARD_HOST = "0.0.0.0";
    private static final int DEFAULT_DASHBOARD_PORT = 8020;

    public TomlConfigSource(String path) {
        this.path = path;
    }

    @Override
    public AppConfig load() {
        Toml root = TomlUtils.readToml(path);
        AppConfig.Builder builder = AppConfig.builder();

        parseRoot(builder, root);
        parseLogConfig(builder, root);
        parseDashboard(builder, root);
        parseTls(builder, root);
        parsePortRange(builder, root);
        parseAccessTokens(builder, root);

        return builder.build();
    }

    private void parseRoot(AppConfig.Builder builder, Toml root) {
        String serverAddrValue = root.getString("serverAddr", DEFAULT_HOST);
        if (StringUtils.hasText(serverAddrValue)) {
            builder.serverAddr(serverAddrValue.trim());
        }

        Long serverPortValue = root.getLong("serverPort", (long) DEFAULT_BIND_PORT);
        validatePort(serverPortValue.intValue());
        builder.serverPort(serverPortValue.intValue());

        List<String> baseDomains = root.getList("baseDomains", new ArrayList<>());
        builder.baseDomains(new HashSet<>(baseDomains));

        Long httpProxyPort = root.getLong("httpProxyPort", 80L);
        int httpPort = httpProxyPort.intValue();
        validatePort(httpPort);
        builder.httpProxyPort(httpPort);

        Long httpsProxyPort = root.getLong("httpsProxyPort", 443L);
        int httpsPort = httpsProxyPort.intValue();
        validatePort(httpsPort);
        builder.httpsProxyPort(httpsPort);
    }

    private void parseLogConfig(AppConfig.Builder builder, Toml root) {
        LogConfig logConfig = LogUtils.parseLogConfig(root.getTable("log"), true);
        if (logConfig != null) {
            builder.logConfig(logConfig);
        }
    }

    private void parseDashboard(AppConfig.Builder builder, Toml root) {
        Toml dash = root.getTable("dashboard");
        if (dash != null) {
            Boolean enable = dash.getBoolean("enable", false);
            if (enable != null && enable) {
                String addr = dash.getString("addr", DEFAULT_DASHBOARD_HOST);
                Long port = dash.getLong("port", (long) DEFAULT_DASHBOARD_PORT);
                String username = dash.getString("username");
                String password = dash.getString("password");
                Boolean reset = dash.getBoolean("reset", false);

                if (!StringUtils.hasText(username)) {
                    throw new IllegalArgumentException("请配置Dashboard用户名");
                }
                if (!StringUtils.hasText(password)) {
                    throw new IllegalArgumentException("请配置Dashboard密码");
                }

                Dashboard dashboard = new Dashboard(
                        true, username, password, addr, port.intValue(), reset
                );
                builder.dashboard(dashboard);
            }
        }
    }

    private void parseTls(AppConfig.Builder builder, Toml root) {
        Toml tlsTable = root.getTable("tls");
        if (tlsTable != null) {
            Boolean enable = tlsTable.getBoolean("enable", false);
            String certPath = tlsTable.getString("certPath");
            String keyPath = tlsTable.getString("keyPath");
            String storePassPath = tlsTable.getString("storePassPath");
            
            TLSConfig tlsConfig = new TLSConfig(enable, certPath, keyPath, storePassPath);
            builder.tls(tlsConfig);
        }
    }

    private void parsePortRange(AppConfig.Builder builder, Toml root) {
        Toml range = root.getTable("port_range");
        if (range != null) {
            Long start = range.getLong("start", 1024L);
            Long end = range.getLong("end", 49151L);
            PortRange portRange = new PortRange(
                    start.intValue(), end.intValue()
            );
            builder.portRange(portRange);
        } else {
            PortRange portRange = new PortRange(1, 65535);
            builder.portRange(portRange);
        }
    }

    private void parseAccessTokens(AppConfig.Builder builder, Toml root) {
        List<Toml> accessTokenTables = root.getTables("access_tokens");
        if (accessTokenTables == null) {
            return;
        }

        List<AccessToken> accessTokens = new CopyOnWriteArrayList<>();
        Set<String> tokenTemp = new HashSet<>();

        for (Toml tokenTable : accessTokenTables) {
            String name = tokenTable.getString("name");
            String token = tokenTable.getString("token");
            Long maxClients = tokenTable.getLong("maxClients");

            if (!StringUtils.hasText(name)) {
                throw new IllegalArgumentException("必须指定AccessToken的描述！");
            }
            if (!StringUtils.hasText(token)) {
                throw new IllegalArgumentException("必须指定AccessToken的令牌！");
            }
            if (maxClients == null || maxClients <= 0) {
                throw new IllegalArgumentException("AccessToken的最大客户端数必须大于0！");
            }
            if (tokenTemp.contains(token)) {
                throw new IllegalArgumentException("AccessToken令牌冲突，不能存在重复的令牌！ " + token);
            }

            AccessToken accessToken = new AccessToken(name, token, maxClients.intValue());
            accessTokens.add(accessToken);
            tokenTemp.add(token);
        }

        builder.accessTokens(accessTokens);
    }

    @Override
    public ConfigSourceType getSourceType() {
        return ConfigSourceType.TOML;
    }

    public String getPath() {
        return path;
    }

    private void validatePort(int port) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("端口号必须在1-65535范围内: " + port);
        }
    }
}
