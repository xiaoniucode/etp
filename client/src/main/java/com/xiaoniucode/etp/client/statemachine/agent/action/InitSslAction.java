package com.xiaoniucode.etp.client.statemachine.agent.action;

import com.xiaoniucode.etp.client.config.AppConfig;
import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.agent.ClientEvent;
import com.xiaoniucode.etp.client.statemachine.agent.ClientState;
import com.xiaoniucode.etp.core.tls.SslContextFactory;
import io.netty.handler.ssl.SslContext;

public class InitSslAction extends AgentBaseAction {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(InitSslAction.class);

    @Override
    protected void doExecute(ClientState from, ClientState to, ClientEvent event, AgentContext ctx) {
        logger.debug("检查是否有必要初始化 TLS证书");
        try {
            AppConfig config = ctx.getConfig();
            if (config.getTlsConfig().getEnable()) {
                logger.debug("初始化 SSL上下文");
                SslContext tlsContext = SslContextFactory.createClientSslContext(config.getTlsConfig());
                ctx.setTlsContext(tlsContext);
            }
            ctx.getStateMachine().fireEvent(ctx.getState(),ClientEvent.SSL_INITIALIZED,ctx);
        } catch (Exception e) {
            logger.error("SSL 初始化失败", e);
            ctx.setSslInitialized(false);
            ctx.getStateMachine().fireEvent(ctx.getState(),ClientEvent.STOP,ctx);
        }
    }
}
