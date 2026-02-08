package com.xiaoniucode.etp.server.event;

import com.xiaoniucode.etp.core.enums.ClientType;
import com.xiaoniucode.etp.core.notify.Event;
import com.xiaoniucode.etp.server.manager.domain.AgentSession;
import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 代理客户端注册成功事件
 */
@Getter
public class AgentLoginEvent extends Event {
    /**
     * 是否是新用户
     */
    private final boolean isNew;
    private final String clientId;
    private final String name;
    private final ClientType clientType;
    private final String token;
    private final String arch;
    private final String os;
    private final String version;

    public AgentLoginEvent(boolean isNew, AgentSession session) {
        this.isNew = isNew;
        this.clientId = session.getClientId();
        this.name = session.getName();
        this.clientType = session.getClientType();
        this.token = session.getToken();
        this.arch = session.getArch();
        this.os = session.getOs();
        this.version = session.getVersion();
    }
}
