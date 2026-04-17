/*
 *    Copyright 2026 xiaoniucode
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.xiaoniucode.etp.server.web.support.store;

import com.xiaoniucode.etp.server.config.domain.TokenConfig;
import com.xiaoniucode.etp.server.exceptions.EtpException;
import com.xiaoniucode.etp.server.store.TokenStore;
import com.xiaoniucode.etp.server.web.entity.AccessTokenDO;
import com.xiaoniucode.etp.server.web.repository.AccessTokenRepository;
import com.xiaoniucode.etp.server.web.support.store.converter.TokenStoreConvert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Primary
public class CompositeTokenStore implements TokenStore {
    private final Logger logger = LoggerFactory.getLogger(CompositeTokenStore.class);
    @Autowired
    private MultiLevelCache multiLevelCache;
    @Autowired
    private AccessTokenRepository accessTokenRepository;
    @Autowired
    private TokenStoreConvert tokenStoreConvert;
    private final String CACHE_NAME = "access_token";

    @Override
    public TokenConfig findByToken(String token) {
        logger.debug("根据 token 查询令牌配置，token: {}", token);
        String cacheKey = "token:" + token;
        return multiLevelCache.get(CACHE_NAME, cacheKey, () -> {
                    AccessTokenDO accessTokenDO = accessTokenRepository.findByToken(token);
                    if (accessTokenDO != null) {
                        return tokenStoreConvert.toTokenConfig(accessTokenDO);
                    }
                    return null;
                }
        );
    }

    @Override
    public TokenConfig getByToken(String token) {
        logger.debug("根据 token 获取令牌配置，token: {}", token);
        return findByToken(token);
    }

    @Override
    public List<TokenConfig> findAll() {
        logger.debug("查询所有令牌配置");
        String cacheKey = "all";
        return multiLevelCache.get(CACHE_NAME, cacheKey, () -> {
            List<AccessTokenDO> accessTokenDOs = accessTokenRepository.findAll();
            return tokenStoreConvert.toTokenConfigList(accessTokenDOs);
        });
    }

    @Override
    public boolean existsByToken(String token) {
        logger.debug("检查 token 是否存在，token: {}", token);
        String cacheKey = "exists:token:" + token;
        return multiLevelCache.get(CACHE_NAME, cacheKey, () ->
                accessTokenRepository.existsByToken(token)
        );
    }

    @Override
    public boolean existsByName(String name) {
        logger.debug("检查名称是否存在，name: {}", name);
        String cacheKey = "exists:name:" + name;
        return multiLevelCache.get(CACHE_NAME, cacheKey, () ->
                accessTokenRepository.existsByName(name)
        );
    }

    private void clearTokenCache(String token, String name) {
        multiLevelCache.evict(CACHE_NAME, "all");
        multiLevelCache.evict(CACHE_NAME, "token:" + token);
        multiLevelCache.evict(CACHE_NAME, "exists:token:" + token);
        if (name != null) {
            multiLevelCache.evict(CACHE_NAME, "exists:name:" + name);
        }
    }

    @Override
    public TokenConfig add(TokenConfig token) throws EtpException {
        logger.debug("添加令牌配置，name: {}, token: {}", token.getName(), token.getToken());
        clearTokenCache(token.getToken(), token.getName());
        return token;
    }

    @Override
    public TokenConfig update(TokenConfig tokenConfig) throws EtpException {
        logger.debug("更新令牌配置，name: {}, token: {}", tokenConfig.getName(), tokenConfig.getToken());
        clearTokenCache(tokenConfig.getToken(), tokenConfig.getName());
        return tokenConfig;
    }

    @Override
    public TokenConfig deleteByToken(String token) {
        logger.debug("删除令牌配置，token: {}", token);
        TokenConfig tokenConfig = findByToken(token);
        clearTokenCache(token, null);
        return tokenConfig;
    }
}
