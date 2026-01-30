package com.xiaoniucode.etp.server.manager.domain;

import com.xiaoniucode.etp.server.enums.ClientType;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgentSession {
    private String sessionId;
    /**
     * 唯一标识一台设备指纹ID，对于临时客户端clientId和sessionId 一样
     */
    private String clientId;
    private ClientType clientType;
    private Channel control;
    private String token;
    private String arch;
    private String os;
    /**
     * 最后心跳时间
     */
    private long lastHeartbeat;

    public AgentSession(String clientId, String token, Channel control) {
        this.clientId = clientId;
        this.token = token;
        this.control = control;
    }
}
