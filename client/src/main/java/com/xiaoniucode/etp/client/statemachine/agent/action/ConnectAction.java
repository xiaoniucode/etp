package com.xiaoniucode.etp.client.statemachine.agent.action;

import com.xiaoniucode.etp.client.config.AppConfig;
import com.xiaoniucode.etp.client.config.domain.RetryConfig;
import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.agent.AgentEvent;
import com.xiaoniucode.etp.client.statemachine.agent.AgentState;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 与服务端建立连接
 */
public class ConnectAction extends AgentBaseAction {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ConnectAction.class);
    private final AtomicInteger retryCount = new AtomicInteger(0);
    private AppConfig appConfig;
    private AgentContext agentContext;

    @Override
    protected void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext ctx) {
        try {
            this.appConfig = ctx.getConfig();
            this.agentContext = ctx;

            Bootstrap controlBootstrap = ctx.getControlBootstrap();

            ChannelFuture channelFuture = controlBootstrap.connect(appConfig.getServerAddr(), appConfig.getServerPort());
            channelFuture.addListener((ChannelFutureListener) f -> {
                if (f.isSuccess()) {
                    logger.debug("成功连接到服务器");
                    retryCount.set(0);
                    ctx.setControl(channelFuture.channel());
                    ctx.fireEvent(AgentEvent.CONNECT_SUCCESS);
                } else {
                    handleFailure();
                }
            });
        } catch (Exception e) {
            handleFailure();
        }
    }

    private void handleFailure() {
        RetryConfig retry = appConfig.getAuthConfig().getRetry();
        if (retryCount.get() >= retry.getMaxRetries()) {
            logger.error("无法连接到服务器");
            agentContext.fireEvent(AgentEvent.STOP);
            return;
        }
        int currentRetry = retryCount.getAndIncrement();
        long delay = calculateDelay();
        logger.debug("连接失败，第{}次重连将在{}秒后执行", currentRetry + 1, delay);

        agentContext.getControlWorkerGroup().schedule(() -> {
            logger.debug("开始执行第{}次重连", currentRetry + 1);
            doExecute(null, null, null, agentContext);
        }, delay, TimeUnit.SECONDS);
    }


    /**
     * 指数退避
     *
     * @return 时间（秒）
     */
    private long calculateDelay() {
        RetryConfig retry = appConfig.getAuthConfig().getRetry();
        int retries = retryCount.get();
        if (retries == 0) {
            return retry.getInitialDelay();
        }
        // 指数退避 + 随机抖动(±30%)
        long delay = Math.min((1L << retries), retry.getMaxDelay());
        long jitter = (long) (delay * 0.3 * (Math.random() * 2 - 1));
        return Math.min(delay + jitter, retry.getMaxDelay());
    }
}