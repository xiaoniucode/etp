package com.xiaoniucode.etp.core.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Getter
public class BasicAuthConfig {
    @Setter
    private boolean enable = false;
    private final Set<HttpUser> users = new CopyOnWriteArraySet<>();

    public BasicAuthConfig(boolean enable, Set<HttpUser> users) {
        this.enable = enable;
        if (users != null && !users.isEmpty()) {
            this.users.addAll(users);
        }
    }

}
