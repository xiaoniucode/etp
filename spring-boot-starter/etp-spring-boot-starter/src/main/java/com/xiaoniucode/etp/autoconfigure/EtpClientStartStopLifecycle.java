package com.xiaoniucode.etp.autoconfigure;

import com.xiaoniucode.etp.client.TunnelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

/**
 * @author liuxin
 */
public class EtpClientStartStopLifecycle implements SmartLifecycle {
    private final Logger logger = LoggerFactory.getLogger(EtpClientStartStopLifecycle.class);
    private final EtpClientProperties properties;
    private volatile boolean running = false;
    private TunnelClient tunnelClient;

    public EtpClientStartStopLifecycle(EtpClientProperties properties) {
        this.properties = properties;
    }

    @Override
    public void start() {
        if (properties.isTls()) {
            System.setProperty("client.truststore.path", properties.getTruststore().getPath());
            System.setProperty("client.truststore.storePass", properties.getTruststore().getStorePass());
        }
        tunnelClient = new TunnelClient(properties.getServerAddr(), properties.getServerPort(), properties.getSecretKey(), properties.isTls());
        tunnelClient.setMaxDelaySec(properties.getMaxDelaySec());
        tunnelClient.setInitialDelaySec(properties.getInitialDelaySec());
        tunnelClient.setMaxRetries(properties.getMaxRetries());
        tunnelClient.start();
        running = true;
        logger.info("Etp client start success: {}：{}", properties.getServerAddr(), properties.getServerPort());
    }

    @Override
    public void stop() {
        if (running && tunnelClient != null) {
            tunnelClient.stop();
            logger.info("etp client stopped");
        }
        System.clearProperty("client.truststore.path");
        System.clearProperty("client.truststore.storePass");
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
