package com.xiaoniucode.etp.server.manager.domain;

import com.xiaoniucode.etp.core.enums.ClientType;
import com.xiaoniucode.etp.server.timer.WheelTimer;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class AgentSession {
    private final String sessionId;
    private final String clientId;
    private final String name;
    private final ClientType clientType;
    private final Channel control;
    private final String token;
    private final String arch;
    private final String os;
    private final String version;
    private final boolean isNew;
    private long lastHeartbeat;
    private WheelTimer.TimeoutHandle timeoutHandle;

    private AgentSession(String sessionId, String clientId, String name,
                         ClientType clientType, Channel control, String token,
                         String arch, String os, String version,boolean isNew) {
        this.sessionId = sessionId;
        this.clientId = clientId;
        this.name = name;
        this.clientType = clientType;
        this.control = control;
        this.token = token;
        this.arch = arch;
        this.os = os;
        this.version = version;
        this.lastHeartbeat = System.currentTimeMillis();
        this.isNew=isNew;
    }

    public static AgentSessionBuilder builder() {
        return new AgentSessionBuilder();
    }

    public static class AgentSessionBuilder {
        private String sessionId;
        private String clientId;
        private String name;
        private ClientType clientType;
        private Channel control;
        private String token;
        private String arch;
        private String os;
        private String version;
        private boolean isNew;

        private AgentSessionBuilder() {
        }

        public AgentSessionBuilder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public AgentSessionBuilder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public AgentSessionBuilder name(String name) {
            this.name = name;
            return this;
        }

        public AgentSessionBuilder clientType(ClientType clientType) {
            this.clientType = clientType;
            return this;
        }

        public AgentSessionBuilder control(Channel control) {
            this.control = control;
            return this;
        }

        public AgentSessionBuilder token(String token) {
            this.token = token;
            return this;
        }

        public AgentSessionBuilder arch(String arch) {
            this.arch = arch;
            return this;
        }

        public AgentSessionBuilder os(String os) {
            this.os = os;
            return this;
        }

        public AgentSessionBuilder version(String version) {
            this.version = version;
            return this;
        }
        public AgentSessionBuilder isNew(boolean isNew) {
            this.isNew = isNew;
            return this;
        }
        public AgentSession build() {
            return new AgentSession(
                    sessionId, clientId, name, clientType, control,
                    token, arch, os, version,isNew
            );
        }
    }
}