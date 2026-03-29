package com.xiaoniucode.etp.core.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Getter
public class BasicAuthConfig {
    @Setter
    private boolean enabled;
    private final Set<HttpUser> users = new CopyOnWriteArraySet<>();
    private final Map<String, HttpUser> cache = new ConcurrentHashMap<>();

    public BasicAuthConfig(boolean enabled, Set<HttpUser> users) {
        this.enabled = enabled;
        if (users != null && !users.isEmpty()) {
            for (HttpUser user : users) {
                this.users.add(user);
                cache.put(user.getUsername(), user);
            }
        }
    }

    public boolean check(String user, String pass) {
        HttpUser httpUser = cache.get(user);
        return httpUser != null && httpUser.getPassword().equals(pass);
    }
}
