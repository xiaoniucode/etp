package com.xiaoniucode.etp.server.config.domain;

import com.xiaoniucode.etp.common.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccessToken {
    public static final int UNLIMITED_CLIENTS = -1;
    private String name;
    private String token;
    private Integer maxClients;

    public AccessToken(String name, String token, Integer maxClients) {
        check(name, token, maxClients);
        this.name = name;
        this.token = token;
        this.maxClients = maxClients;
    }

    private void check(String name, String token, Integer maxClients) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("AccessToken 名称不能为空");
        }
        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("AccessToken 令牌不能为空");
        }
        // null 或 <= 0 都表示不限制
        if (maxClients == null || maxClients <= 0) {
            this.maxClients = UNLIMITED_CLIENTS;
        } else {
            this.maxClients = maxClients;
        }
    }
}