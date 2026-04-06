package com.xiaoniucode.etp.server.web.controller.agent.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class AgentDTO implements Serializable {
    private String id;
    private String token;
    private Boolean isOnline;
    private String name;
    private String os;
    private String arch;
    private String version;
    private Integer agentType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
