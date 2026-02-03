package com.xiaoniucode.etp.server.manager;


import com.xiaoniucode.etp.server.config.domain.AccessTokenInfo;
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
    private static final Map<String, AccessTokenInfo> cache = new ConcurrentHashMap<>();

    public void addAccessToken(AccessTokenInfo accessTokenInfo) {
        if (cache.containsKey(accessTokenInfo.getToken())) {
            logger.warn("Token 令牌已经存在");
            return;
        }
        cache.put(accessTokenInfo.getToken(), accessTokenInfo);
    }

    public void addAccessTokens(Collection<AccessTokenInfo> accessTokenInfos) {
        for (AccessTokenInfo accessToken : accessTokenInfos) {
            addAccessToken(accessToken);
        }
    }

    public boolean containsToken(String token) {
        return getAccessToken(token) != null;
    }

    public AccessTokenInfo getAccessToken(String token) {
        return cache.get(token);
    }

    public boolean hasToken(String token) {
        return cache.containsKey(token);
    }
}
