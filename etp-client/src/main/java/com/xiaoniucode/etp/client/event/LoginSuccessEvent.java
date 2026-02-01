package com.xiaoniucode.etp.client.event;

public class LoginSuccessEvent {
    private final String clientId;
    public LoginSuccessEvent(String clientId) {
        this.clientId=clientId;
    }

    public String getClientId() {
        return clientId;
    }
}
