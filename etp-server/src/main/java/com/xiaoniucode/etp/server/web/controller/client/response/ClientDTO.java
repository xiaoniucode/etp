package com.xiaoniucode.etp.server.web.controller.client.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;
@Getter
@Setter
@ToString
public class ClientDTO implements Serializable {
    private String id;
    private Boolean isOnline;
    private String name;
    private String os;
    private String arch;
    private String version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
