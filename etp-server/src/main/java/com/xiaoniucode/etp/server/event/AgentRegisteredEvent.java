package com.xiaoniucode.etp.server.event;

import com.xiaoniucode.etp.core.notify.Event;
import com.xiaoniucode.etp.server.manager.domain.AgentSession;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 代理客户端注册成功事件
 */
@Getter
@AllArgsConstructor
public class AgentRegisteredEvent extends Event {
    private final AgentSession agentSession;
}
