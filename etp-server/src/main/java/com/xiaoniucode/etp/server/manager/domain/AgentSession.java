package com.xiaoniucode.etp.server.manager.domain;

import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgentSession {
    private String sessionId;
    private Channel control;
    private String token;
    private String arch;
    private String os;
    /**
     * 最后心跳时间
     */
    private long lastHeartbeat;

    public AgentSession(Channel control, String token) {
        this.control = control;
        this.token = token;
    }
}
