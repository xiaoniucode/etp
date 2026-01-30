package com.xiaoniucode.etp.server.manager;

import com.xiaoniucode.etp.server.manager.domain.AccessTokenInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AccessTokenManager {
    private static final Map<String, AccessTokenInfo> cache = new ConcurrentHashMap<>();

    public static AccessTokenInfo getAccessToken(String token) {
        return cache.get(token);
    }

    public static boolean hasToken(String token) {
        return cache.containsKey(token);
    }
}
