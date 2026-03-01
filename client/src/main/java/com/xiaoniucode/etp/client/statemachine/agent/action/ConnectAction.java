package com.xiaoniucode.etp.client.statemachine.agent.action;

import com.xiaoniucode.etp.client.config.AppConfig;
import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.agent.AgentEvent;
import com.xiaoniucode.etp.client.statemachine.agent.AgentState;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

public class ConnectAction extends AgentBaseAction {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ConnectAction.class);

    @Override
    protected void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext ctx) {
        try {
            logger.debug("连接到隧道服务器");
            AppConfig config = ctx.getConfig();
            Bootstrap controlBootstrap = ctx.getControlBootstrap();

            ChannelFuture channelFuture = controlBootstrap.connect(config.getServerAddr(), config.getServerPort());
            channelFuture.addListener((ChannelFutureListener) f -> {
                if (f.isSuccess()) {
                    Channel control = channelFuture.channel();
                    ctx.setControl(control);
                    // 触发连接成功事件
                    logger.debug("成功连接到服务器");
                    ctx.getStateMachine().fireEvent(AgentState.CONNECTING, AgentEvent.CONNECT_SUCCESS, ctx);
                } else {
                    logger.debug("无法连接到服务器");
                    // 触发连接失败事件
                    ctx.getStateMachine().fireEvent(AgentState.CONNECTING, AgentEvent.CONNECT_FAILURE, ctx);
                }
            });
        } catch (Exception e) {
            logger.error("连接失败", e);
            ctx.getStateMachine().fireEvent(AgentState.CONNECTING, AgentEvent.CONNECT_FAILURE, ctx);
        }
    }

//    private void scheduleReconnect() {
//        if (retryCount.get() >= config.getAuthConfig().getRetry().getMaxRetries()) {
//            logger.error("达到最大重试次数，停止重连");
//            this.stop();
//            return;
//        }
//        int retries = retryCount.getAndIncrement();
//        long delay = calculateDelay();
//        logger.error("连接失败，第{}次重连将在{}秒后执行", retries + 1, delay);
//        // controlWorkerGroup.schedule(this::connectTunnelServer, delay, TimeUnit.SECONDS);
//    }
//
//    /**
//     * 指数退避算法，计算重连延迟时间
//     *
//     * @return 时间（秒）
//     */
//    private long calculateDelay() {
//        RetryConfig retry = config.getAuthConfig().getRetry();
//        int retries = retryCount.get();
//        if (retries == 0) {
//            return retry.getInitialDelay();
//        }
//        // 指数退避 + 随机抖动(±30%)
//        long delay = Math.min((1L << retries), retry.getMaxDelay());
//        long jitter = (long) (delay * 0.3 * (Math.random() * 2 - 1));
//        return Math.min(delay + jitter, retry.getMaxDelay());
//    }
}