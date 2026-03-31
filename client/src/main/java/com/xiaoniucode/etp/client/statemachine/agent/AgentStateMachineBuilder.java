/*
 *    Copyright 2026 xiaoniucode
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.xiaoniucode.etp.client.statemachine.agent;

import com.alibaba.cola.statemachine.StateMachine;
import com.alibaba.cola.statemachine.StateMachineFactory;
import com.alibaba.cola.statemachine.builder.StateMachineBuilder;
import com.alibaba.cola.statemachine.builder.StateMachineBuilderFactory;
import com.xiaoniucode.etp.client.statemachine.agent.action.*;
import com.xiaoniucode.etp.client.statemachine.agent.action.AuthAction;
import com.xiaoniucode.etp.client.statemachine.agent.action.AuthResponseAction;
import com.xiaoniucode.etp.client.statemachine.agent.action.tunnel.CreateConnPoolAction;
import com.xiaoniucode.etp.client.statemachine.agent.action.tunnel.CreateNewConnAction;

/**
 * 客户端代理状态机构建器
 * 负责构建和配置客户端代理的状态机，管理客户端的各种状态转换
 */
public class AgentStateMachineBuilder {

    /**
     * 状态机 ID
     */
    private static final String MACHINE_ID = "clientStateMachine";

    /**
     * 状态机持有者
     */
    private static class StateMachineHolder {
        /**
         * 状态机实例
         */
        private static final StateMachine<AgentState, AgentEvent, AgentContext> INSTANCE = build();

        /**
         * 构建状态机
         *
         * @return 构建好的状态机实例
         */
        private static StateMachine<AgentState, AgentEvent, AgentContext> build() {
            StateMachineBuilder<AgentState, AgentEvent, AgentContext> builder = StateMachineBuilderFactory.create();


            CheckConfigAction checkConfigAction = new CheckConfigAction();
            InitSslAction initSslAction = new InitSslAction();
            ConnectAction connectAction = new ConnectAction();
            AuthAction authAction = new AuthAction();
            AuthResponseAction authResponseAction = new AuthResponseAction();
            AuthSuccessAction authSuccessAction = new AuthSuccessAction();
            NetworkErrorAction networkErrorAction = new NetworkErrorAction();
            GoawayAction goawayAction = new GoawayAction();
            CreateConnPoolAction createTunnelPoolAction = new CreateConnPoolAction();
            TunnelCreateRespAction tunnelCreateRespAction = new TunnelCreateRespAction();
            ProxyCreationResponseAction proxyCreationResponseAction = new ProxyCreationResponseAction();
            ErrorAction errorAction = new ErrorAction();
            CreateNewConnAction createNewConnAction = new CreateNewConnAction();
            DisconnectedAction disconnectedAction = new DisconnectedAction();
            RetryConnAction retryConnAction = new RetryConnAction();

            builder.externalTransition()
                    .from(AgentState.IDLE)
                    .to(AgentState.CONNECTING)
                    .on(AgentEvent.START)
                    .perform(checkConfigAction);

            // 配置检查
            builder.internalTransition()
                    .within(AgentState.CONNECTING)
                    .on(AgentEvent.CONFIG_CHECKED)
                    .perform(initSslAction);

            // SSL 初始化
            builder.internalTransition()
                    .within(AgentState.CONNECTING)
                    .on(AgentEvent.SSL_INITIALIZED)
                    .perform(connectAction);

            // TCP 连接成功
            builder.internalTransition()
                    .within(AgentState.CONNECTING)
                    .on(AgentEvent.CONNECT_SUCCESS)
                    .perform(authAction);

            // 认证响应
            builder.internalTransition()
                    .within(AgentState.CONNECTING)
                    .on(AgentEvent.AUTH_RESPONSE)
                    .perform(authResponseAction);
            // 认证成功
            builder.externalTransition()
                    .from(AgentState.CONNECTING)
                    .to(AgentState.CONNECTED)
                    .on(AgentEvent.AUTH_SUCCESS)
                    .perform(authSuccessAction);
            // 运行中出现网络错误
            builder.externalTransition()
                    .from(AgentState.CONNECTED)
                    .to(AgentState.DISCONNECTED)
                    .on(AgentEvent.NETWORK_ERROR)
                    .perform(networkErrorAction);
            // 断开连接
            builder.externalTransition()
                    .from(AgentState.CONNECTED)
                    .to(AgentState.DISCONNECTED)
                    .on(AgentEvent.DISCONNECT)
                    .perform(disconnectedAction);
            // 连接断开，尝试重连
            builder.externalTransitions()
                    .fromAmong(AgentState.DISCONNECTED)
                    .to(AgentState.CONNECTING)
                    .on(AgentEvent.RETRY)
                    .perform(connectAction);
            // 连接失败 尝试重连
            builder.internalTransition()
                    .within(AgentState.CONNECTING)
                    .on(AgentEvent.CONNECT_FAILURE)
                    .perform(retryConnAction);
            //首次连接失败 尝试重试
            builder.internalTransition()
                    .within(AgentState.CONNECTING)
                    .on(AgentEvent.RETRY)
                    .perform(connectAction);

            // 处理创建隧道池请求
            builder.internalTransition()
                    .within(AgentState.CONNECTED)
                    .on(AgentEvent.CREATE_TUNNEL_POOL)
                    .perform(createTunnelPoolAction);

            // 处理隧道创建响应
            builder.internalTransition()
                    .within(AgentState.CONNECTED)
                    .on(AgentEvent.CREATE_TUNNEL_POOL_RESP)
                    .perform(tunnelCreateRespAction);

            // 处理代理创建响应
            builder.internalTransition()
                    .within(AgentState.CONNECTED)
                    .on(AgentEvent.PROXY_CREATE_RESP)
                    .perform(proxyCreationResponseAction);

            // 创建新连接
            builder.internalTransition()
                    .within(AgentState.CONNECTED)
                    .on(AgentEvent.CREATE_NEW_CONN)
                    .perform(createNewConnAction);

            // 处理错误
            builder.internalTransition()
                    .within(AgentState.CONNECTED)
                    .on(AgentEvent.ERROR)
                    .perform(errorAction);

            // 停止客户端
            builder.externalTransitions()
                    .fromAmong(AgentState.IDLE, AgentState.CONNECTING,
                            AgentState.CONNECTED, AgentState.DISCONNECTED,
                            AgentState.FAILED)
                    .to(AgentState.SHUTDOWN)
                    .on(AgentEvent.STOP)
                    .perform(goawayAction);

            builder.build(MACHINE_ID);
            return StateMachineFactory.get(MACHINE_ID);
        }
    }

    /**
     * 获取状态机实例
     *
     * @return 状态机实例
     */
    public static StateMachine<AgentState, AgentEvent, AgentContext> getStateMachine() {
        return StateMachineHolder.INSTANCE;
    }
}