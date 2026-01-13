package com.xiaoniucode.etp.autoconfigure;

import com.xiaoniucode.etp.client.manager.ChannelManager;
import com.xiaoniucode.etp.client.ProxyRegisterClient;
import com.xiaoniucode.etp.client.TunnelClient;
import com.xiaoniucode.etp.common.StringUtils;
import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.codec.TunnelMessage;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.env.Environment;

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
    private final ProxyRegisterClient proxyRegisterClient;

    public EtpClientStartStopLifecycle(Environment environment, WebServerPortListener webServerPortListener,
        ProxyRegisterClient proxyRegisterClient, EtpClientProperties properties) {
        this.environment = environment;
        this.properties = properties;
        this.proxyRegisterClient = proxyRegisterClient;
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
        tunnelClient = new TunnelClient(properties.getServerAddr(), properties.getServerPort(), secretKey, properties.isTls());
        tunnelClient.setMaxDelaySec(properties.getMaxDelaySec());
        tunnelClient.setInitialDelaySec(properties.getInitialDelaySec());
        tunnelClient.setMaxRetries(properties.getMaxRetries());
        tunnelClient.start();
        //等代理客户端成功连接到服务端的时候再注册端口映射
        tunnelClient.onConnectSuccessListener((callback) -> {
            int localPort = webServerPortListener.getActualPort();
            String appName = environment.getProperty("spring.application.name", "Spring-Boot");
            TunnelMessage.ProxyRequest request = TunnelMessage
                .ProxyRequest
                .newBuilder()
                .setLocalPort(localPort)
                .setAutoStart(properties.isAutoStart())
                .setProxyName(appName)
                .setRemotePort(properties.getRemotePort())
                .setProtocol(properties.getProtocol().name())
                .build();
            proxyRegisterClient.registerProxy(request);
        });
        running = true;
    }

    @Override
    public void stop() {
        //注销端口映射
        Channel controlChannel = ChannelManager.getControlChannel();
        if (controlChannel != null) {
            //发送下线消息
            Integer proxyId = controlChannel.attr(EtpConstants.PROXY_ID).get();
            proxyRegisterClient.unregisterProxy(proxyId);
        } else {
            logger.warn("control channel is null");
        }
        System.clearProperty("client.truststore.path");
        System.clearProperty("client.truststore.storePass");
        //停掉客户端
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
