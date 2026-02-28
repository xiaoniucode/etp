package com.xiaoniucode.etp.client.event;

import lombok.Getter;

@Getter
public class LoginSuccessEvent {
    private final String clientId;
    private final int connectionId;
    public LoginSuccessEvent(String clientId, int connectionId) {
        this.clientId=clientId;
        this.connectionId=connectionId;
    }
}
