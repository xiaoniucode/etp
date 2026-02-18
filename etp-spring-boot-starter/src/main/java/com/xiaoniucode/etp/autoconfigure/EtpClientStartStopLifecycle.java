package com.xiaoniucode.etp.autoconfigure;

import com.xiaoniucode.etp.client.config.AppConfig;
import com.xiaoniucode.etp.client.config.DefaultAppConfig;
import com.xiaoniucode.etp.client.TunnelClient;
import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.domain.TlsConfig;
import com.xiaoniucode.etp.core.enums.ClientType;
import com.xiaoniucode.etp.core.enums.ProxyStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.env.Environment;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * etp客户端启动、停止生命周期
 *
 * @author liuxin
 */
public class EtpClientStartStopLifecycle implements SmartLifecycle {
    private final Logger logger = LoggerFactory.getLogger(EtpClientStartStopLifecycle.class);
    private final EtpClientProperties properties;
    private volatile boolean running = false;
    private TunnelClient tunnelClient;
    private final Environment environment;
    private final WebServerPortListener webServerPortListener;


    public EtpClientStartStopLifecycle(Environment environment, WebServerPortListener webServerPortListener, EtpClientProperties properties) {
        this.environment = environment;
        this.properties = properties;
        this.webServerPortListener = webServerPortListener;
    }

    @Override
    public void start() {
        ProxyConfig proxyConfig = new ProxyConfig();
        proxyConfig.setName(environment.getProperty("spring.application.name"));
        proxyConfig.setLocalIp(properties.getLocalIp());
        proxyConfig.setProtocol(properties.getProtocol());
        proxyConfig.setRemotePort(properties.getRemotePort());
        proxyConfig.setLocalPort(webServerPortListener.getActualPort());
        proxyConfig.setStatus(ProxyStatus.OPEN);
        List<ProxyConfig> proxies = new CopyOnWriteArrayList<>();
        proxies.add(proxyConfig);
        AppConfig config = new DefaultAppConfig
                .Builder()
                .serverAddr(properties.getServerAddr())
                .serverPort(properties.getServerPort())
                .clientType(ClientType.WEB_SESSION)
                .tlsConfig(properties.getTls())
                .authConfig(properties.getAuthConfig())
                .proxies(proxies)
                .build();
        tunnelClient = new TunnelClient(config);
        tunnelClient.start();
        running = true;
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
