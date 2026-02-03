package com.xiaoniucode.etp.server.manager;


import com.xiaoniucode.etp.server.config.domain.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AccessTokenManager {
    private final Logger logger = LoggerFactory.getLogger(AccessTokenManager.class);
    /**
     * token --> AccessToken
     */
    private static final Map<String, AccessToken> cache = new ConcurrentHashMap<>();

    public void addAccessToken(AccessToken accessToken) {
        if (cache.containsKey(accessToken.getToken())) {
            logger.warn("Token 令牌已经存在");
            return;
        }
        cache.put(accessToken.getToken(), accessToken);
    }

    public void addAccessTokens(Collection<AccessToken> accessTokens) {
        for (AccessToken accessToken : accessTokens) {
            addAccessToken(accessToken);
        }
    }

    public AccessToken getAccessToken(String token) {
        return cache.get(token);
    }

    public boolean hasToken(String token) {
        return cache.containsKey(token);
    }
}
