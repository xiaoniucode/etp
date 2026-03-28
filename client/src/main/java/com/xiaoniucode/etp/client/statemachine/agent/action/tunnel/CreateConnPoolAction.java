package com.xiaoniucode.etp.client.statemachine.agent.action.tunnel;

import com.xiaoniucode.etp.client.config.AppConfig;
import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.agent.AgentEvent;
import com.xiaoniucode.etp.client.statemachine.agent.AgentState;
import com.xiaoniucode.etp.client.statemachine.agent.action.AgentBaseAction;

/**
 * 预创建数据传输隧道
 */
public class CreateConnPoolAction extends AgentBaseAction {

    private static final int DEFAULT_DIRECT_COUNT = 10;

    @Override
    protected void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext context) {
//        AppConfig config = context.getConfig();
//        boolean hasTls = context.getTlsContext() != null;
//
//        // 创建多路复用隧道
//        TunnelConnCreateHelper.createMultiplexTunnel(context, config, false);
//        if (hasTls) {
//            TunnelConnCreateHelper.createMultiplexTunnel(context, config, true);
//        }
//
//        // 创建独立隧道
//        int count = DEFAULT_DIRECT_COUNT / 2;
//        for (int i = 0; i < count; i++) {
//            TunnelConnCreateHelper.createDirectTunnel(context, config, false);
//            if (hasTls) {
//                TunnelConnCreateHelper.createDirectTunnel(context, config, true);
//            }
//        }
    }
}
