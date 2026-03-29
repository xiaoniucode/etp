package com.xiaoniucode.etp.client.statemachine.agent.action;

import com.xiaoniucode.etp.client.config.AppConfig;
import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.agent.AgentEvent;
import com.xiaoniucode.etp.client.statemachine.agent.AgentState;
import com.xiaoniucode.etp.core.transport.tls.TlsHelper;
import com.xiaoniucode.etp.core.transport.TlsContextHolder;
import io.netty.handler.ssl.SslContext;

public class InitSslAction extends AgentBaseAction {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(InitSslAction.class);

    @Override
    protected void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext ctx) {
        logger.debug("检查是否有必要初始化 TLS证书");
        try {
            AppConfig config = ctx.getConfig();
            if (Boolean.TRUE.equals(config.getTlsConfig().getEnabled())) {
                logger.debug("初始化 SSL上下文");
                SslContext sslContext = TlsHelper.buildSslContext(true, config.getTlsConfig());
                TlsContextHolder.initialize(sslContext);
                ctx.setTlsContext(sslContext);
            }
            ctx.getStateMachine().fireEvent(ctx.getState(), AgentEvent.SSL_INITIALIZED,ctx);
        } catch (Exception e) {
            logger.error("SSL 初始化失败", e);
            ctx.getStateMachine().fireEvent(ctx.getState(), AgentEvent.STOP,ctx);
        }
    }
}
