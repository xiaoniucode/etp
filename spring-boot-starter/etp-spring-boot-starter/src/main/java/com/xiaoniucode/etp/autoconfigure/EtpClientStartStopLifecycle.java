package com.xiaoniucode.etp.autoconfigure;

import com.xiaoniucode.etp.client.config.AppConfig;
import com.xiaoniucode.etp.client.config.DefaultAppConfig;
import com.xiaoniucode.etp.client.ProxyClient;
import com.xiaoniucode.etp.client.TunnelClient;
import com.xiaoniucode.etp.common.utils.StringUtils;
import com.xiaoniucode.etp.core.msg.NewProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.env.Environment;
import java.util.HashSet;
import java.util.Set;

/**
 * etp启动停止生命周期
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
    private final ProxyClient proxyClient;

    public EtpClientStartStopLifecycle(Environment environment, WebServerPortListener webServerPortListener,
                                       ProxyClient proxyClient, EtpClientProperties properties) {
        this.environment = environment;
        this.properties = properties;
        this.proxyClient = proxyClient;
        this.webServerPortListener = webServerPortListener;
    }

    @Override
    public void start() {
        if (properties.isTls()) {
            System.setProperty("client.truststore.path", properties.getTruststore().getPath());
            System.setProperty("client.truststore.storePass", properties.getTruststore().getStorePass());
        }
        String secretKey = properties.getSecretKey();
        if (!StringUtils.hasText(secretKey)) {
            logger.error("必须指定secretKey");
            return;
        }
        AppConfig config = new DefaultAppConfig
                .Builder()
                .serverAddr(properties.getServerAddr())
                .serverPort(properties.getServerPort())
                .secretKey(properties.getSecretKey())
                .tls(properties.isTls())
                .maxDelaySec(properties.getMaxDelaySec())
                .initialDelaySec(properties.getInitialDelaySec())
                .maxRetries(properties.getMaxRetries())
                .build();
        tunnelClient = new TunnelClient(config);
        tunnelClient.start();
        //等代理客户端成功连接到服务端的时候再注册端口映射
        tunnelClient.onConnectSuccessListener((callback) -> {
            int localPort = webServerPortListener.getActualPort();
            String appName = environment.getProperty("spring.application.name", "Spring-Boot");
            Set<String> customDomains = properties.getCustomDomains() != null ? new HashSet<>(properties.getCustomDomains()) : null;
            Set<String> subDomains = properties.getSubDomain() != null ? new HashSet<>(properties.getSubDomain()) : null;
            NewProxy newProxy = ProxyClient.NewProxyBuilder.builder()
                    .localIP(properties.getLocalIP())
                    .localPort(localPort)
                    .status(properties.isAutoStart() ? 1 : 0)
                    .remotePort(properties.getRemotePort())
                    .protocol(properties.getProtocol())
                    .name(appName)
                    .customDomains(customDomains)
                    .subDomains(subDomains)
                    .autoDomain(properties.isAutoDomain())
                    .build();
            proxyClient.registerProxy(newProxy);
        });
        running = true;
    }

    @Override
    public void stop() {
        proxyClient.unregisterProxy();
        if (isRunning() && tunnelClient != null) {
            tunnelClient.stop();
            logger.info("etp client stopped");
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
