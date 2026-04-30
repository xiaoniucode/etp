package com.xiaoniucode.etp.autoconfigure;

import com.xiaoniucode.etp.client.config.AppConfig;
import com.xiaoniucode.etp.client.config.DefaultAppConfig;
import com.xiaoniucode.etp.client.config.domain.AuthConfig;
import com.xiaoniucode.etp.client.TunnelClient;
import com.xiaoniucode.etp.client.config.domain.RetryConfig;
import com.xiaoniucode.etp.client.config.domain.TransportConfig;
import com.xiaoniucode.etp.client.config.domain.ConnectionConfig;
import com.xiaoniucode.etp.core.domain.AccessControlConfig;
import com.xiaoniucode.etp.core.domain.BandwidthConfig;
import com.xiaoniucode.etp.core.domain.BasicAuthConfig;
import com.xiaoniucode.etp.core.domain.RouteConfig;
import com.xiaoniucode.etp.core.domain.HttpUser;
import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.domain.Target;
import com.xiaoniucode.etp.core.domain.TlsConfig;
import com.xiaoniucode.etp.core.domain.TransportCustomConfig;
import com.xiaoniucode.etp.core.enums.AccessControl;
import com.xiaoniucode.etp.core.enums.AgentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.stream.Collectors;

public class EtpClientStartStopLifecycle implements SmartLifecycle {
    private final Logger logger = LoggerFactory.getLogger(EtpClientStartStopLifecycle.class);
    private final EtpClientProperties properties;
    private volatile boolean running = false;
    private TunnelClient tunnelClient;
    private final Environment environment;
    private final WebServerPortListener webServerPortListener;
    private final ResourceLoader resourceLoader;

    public EtpClientStartStopLifecycle(Environment environment, WebServerPortListener webServerPortListener, EtpClientProperties properties, ResourceLoader resourceLoader) {
        this.environment = environment;
        this.properties = properties;
        this.webServerPortListener = webServerPortListener;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void start() {
        // 检查是否启用 ETP 代理
        if (!properties.isEnabled()) {
            logger.info("ETP 代理未启用，跳过启动");
            return;
        }
        
        String applicationName = environment.getProperty("spring.application.name");
        int localPort = webServerPortListener.getActualPort();

        ProxyProperties proxy = properties.getProxy();
        AccessControlProperties accessControl = proxy.getAccessControl();
        BandwidthProperties bandwidth = proxy.getBandwidth();
        TransportCustomProperties transport = proxy.getTransport();
        BasicAuthProperties basicAuth = proxy.getBasicAuth();
        TransportProperties.TlsProperties tls = properties.getTransport().getTls();
        AuthProperties auth = properties.getAuth();
        ConnectionProperties.RetryProperties retry = properties.getConnection().getRetry();

        // 创建并配置 ProxyConfig
        ProxyConfig proxyConfig = new ProxyConfig();
        proxyConfig.setName(applicationName);
        proxyConfig.addTarget(new Target(proxy.getLocalIp(), localPort, 1, applicationName));
        proxyConfig.setEnabled(true);
        proxyConfig.setProtocol(proxy.getProtocol());
        proxyConfig.setRemotePort(proxy.getRemotePort());

        // 配置访问控制
        AccessControlConfig accessControlConfig = new AccessControlConfig(
                accessControl.isEnabled(),
                AccessControl.valueOf(accessControl.getMode().name()),
                accessControl.getAllow(),
                accessControl.getDeny()
        );
        proxyConfig.setAccessControl(accessControlConfig);
        if (StringUtils.hasText(bandwidth.getLimit())||
                StringUtils.hasText(bandwidth.getLimitIn())||
        StringUtils.hasText(bandwidth.getLimitOut())){
            // 配置带宽限制
            BandwidthConfig bandwidthConfig = new BandwidthConfig(
                    bandwidth.getLimit(),
                    bandwidth.getLimitIn(),
                    bandwidth.getLimitOut()
            );

            proxyConfig.setBandwidth(bandwidthConfig);
        }
        // 配置域名信息
        RouteConfig routeConfig = new RouteConfig();
        routeConfig.setAutoDomain(proxy.getAutoDomain());
        routeConfig.getCustomDomains().addAll(proxy.getCustomDomains());
        routeConfig.getSubDomains().addAll(proxy.getSubDomains());
        proxyConfig.setRouteConfig(routeConfig);

        // 配置基础认证
        if (basicAuth.isEnabled() && !basicAuth.getUsers().isEmpty()) {
            Set<HttpUser> users = basicAuth.getUsers().stream()
                    .map(user -> new HttpUser(user.getUser(), user.getPass()))
                    .collect(Collectors.toSet());

            BasicAuthConfig basicAuthConfig = new BasicAuthConfig();
            basicAuthConfig.setEnabled(basicAuth.isEnabled());
            basicAuthConfig.addUsers(users);

            proxyConfig.setBasicAuth(basicAuthConfig);
        }

        // 配置传输
        TransportCustomConfig transportCustomConfig = new TransportCustomConfig(
                transport.isMultiplex(),
                transport.isEncrypt(),
                false
        );
        proxyConfig.setTransport(transportCustomConfig);

        // 配置TLS
        TlsConfig tlsConfig = new TlsConfig(tls.getEnabled());
        tlsConfig.setCertFile(getAbsolutePath(tls.getCertFile()));
        tlsConfig.setKeyFile(getAbsolutePath(tls.getKeyFile()));
        tlsConfig.setCaFile(getAbsolutePath(tls.getCaFile()));
        tlsConfig.setKeyPassword(tls.getKeyPassword());

        // 配置认证
        AuthConfig authConfig = new AuthConfig();
        authConfig.setToken(auth.getToken());
        RetryConfig retryConfig = new RetryConfig();
        retryConfig.setInitialDelay(retry.getInitialDelay());
        retryConfig.setMaxDelay(retry.getMaxDelay());
        retryConfig.setMaxRetries(retry.getMaxRetries());

        // 配置传输
        TransportConfig transportConfig = new TransportConfig();
        transportConfig.setTlsConfig(tlsConfig);

        // 配置连接
        ConnectionConfig connectionConfig = new ConnectionConfig();
        connectionConfig.setRetryConfig(retryConfig);

        // 构建 AppConfig
        AppConfig config = new DefaultAppConfig
                .Builder()
                .serverAddr(properties.getServerAddr())
                .serverPort(properties.getServerPort())
                .agentType(AgentType.EMBEDDED)
                .transportConfig(transportConfig)
                .connectionConfig(connectionConfig)
                .authConfig(authConfig)
                .addProxy(proxyConfig)
                .build();

        tunnelClient = new TunnelClient(config);
        tunnelClient.start();
        running = true;
    }

    public String getAbsolutePath(String location) {
        try {
            if (!StringUtils.hasText(location)){
                return null;
            }
            Resource resource = resourceLoader.getResource(location);
            return resource.getFile().getAbsolutePath();
        } catch (Exception e) {
            throw new RuntimeException("无法加载 TLS 证书文件: " + location, e);
        }
    }

    @Override
    public void stop() {
        if (isRunning() && tunnelClient != null) {
            tunnelClient.stop();
            logger.info("etp 代理服务停止");
        }
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        // 返回最大值，确保最后执行
        return Integer.MAX_VALUE;
    }
}
