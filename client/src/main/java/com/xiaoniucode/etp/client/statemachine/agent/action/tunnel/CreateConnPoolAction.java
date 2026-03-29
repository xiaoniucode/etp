package com.xiaoniucode.etp.client.statemachine.agent.action.tunnel;

import com.xiaoniucode.etp.client.config.AppConfig;
import com.xiaoniucode.etp.client.config.domain.ConnectionPoolConfig;
import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.agent.AgentEvent;
import com.xiaoniucode.etp.client.statemachine.agent.AgentState;
import com.xiaoniucode.etp.client.statemachine.agent.action.AgentBaseAction;

/**
 * 预创建数据传输连接
 */
public class CreateConnPoolAction extends AgentBaseAction {

    @Override
    protected void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext context) {
        AppConfig config = context.getConfig();
        ConnectionPoolConfig connectionPoolConfig = config.getConnectionPoolConfig();
        boolean hasTls = context.getTlsContext() != null;

        if (!connectionPoolConfig.isEnabled()) {
            return;
        }

        // 创建多路复用隧道
        ConnectionPoolConfig.MultiplexPoolConfig multiplexPoolConfig = connectionPoolConfig.getMultiplex();
        if (multiplexPoolConfig.isPlain()) {
            TunnelConnCreateHelper.createMultiplexTunnel(context, config, false);
        }
        if (hasTls && multiplexPoolConfig.isEncrypt()) {
            TunnelConnCreateHelper.createMultiplexTunnel(context, config, true);
        }

        // 创建独立隧道
        ConnectionPoolConfig.DirectPoolConfig directPoolConfig = connectionPoolConfig.getDirect();
        int plainCount = directPoolConfig.getPlainCount();
        int encryptCount = directPoolConfig.getEncryptCount();
        
        for (int i = 0; i < plainCount; i++) {
            TunnelConnCreateHelper.createDirectTunnel(context, config, false);
        }
        if (hasTls) {
            for (int i = 0; i < encryptCount; i++) {
                TunnelConnCreateHelper.createDirectTunnel(context, config, true);
            }
        }
    }
}
