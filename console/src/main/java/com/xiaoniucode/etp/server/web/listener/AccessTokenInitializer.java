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

package com.xiaoniucode.etp.server.web.listener;

import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.config.domain.AuthConfig;
import com.xiaoniucode.etp.server.config.domain.TokenConfig;
import com.xiaoniucode.etp.server.security.TokenManager;
import com.xiaoniucode.etp.server.web.entity.AccessTokenDO;
import com.xiaoniucode.etp.server.web.repository.AccessTokenRepository;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AccessTokenInitializer implements ApplicationRunner {
    private final Logger logger = LoggerFactory.getLogger(AccessTokenInitializer.class);
    @Autowired
    private AccessTokenRepository accessTokenRepository;

    @Autowired
    private TokenManager tokenManager;
    @Resource
    private AppConfig appConfig;

    @Override
    public void run(ApplicationArguments args) {
        try {
            logger.debug("开始初始化访问令牌...");
            int page = 0;
            int size = 100;
            long successCount = 0;
            long failCount = 0;

            Pageable pageable = PageRequest.of(page, size);
            Page<AccessTokenDO> pageResult;
            do {
                pageResult = accessTokenRepository.findAll(pageable);
                logger.debug("处理第 {} 页，每页 {} 条，共 {} 条记录", page + 1, size, pageResult.getTotalElements());
                for (AccessTokenDO accessTokenDO : pageResult.getContent()) {
                    if (tokenManager.addToken(toDomain(accessTokenDO))) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                }
                page++;
                pageable = PageRequest.of(page, size);
            } while (pageResult.hasNext());

            logger.debug("访问令牌初始化到内存完成，成功添加 {} 条记录，失败 {} 条记录", successCount, failCount);

            AuthConfig authConfig = appConfig.getAuthConfig();
            List<TokenConfig> tokens = authConfig.getTokens();
            for (TokenConfig token : tokens) {
                accessTokenRepository.save(toTokenDO(token));
            }
        } catch (Exception e) {
            logger.error("错误", e);
        }

    }

    private AccessTokenDO toTokenDO(TokenConfig info) {
        AccessTokenDO accessTokenDO = new AccessTokenDO();
        accessTokenDO.setName(info.getName());
        accessTokenDO.setToken(info.getToken());
        accessTokenDO.setMaxDevices(info.getMaxDevices());
        accessTokenDO.setMaxConnections(info.getMaxConnections());
        return accessTokenDO;
    }

    private TokenConfig toDomain(AccessTokenDO at) {
        return new TokenConfig(at.getName(), at.getToken(), at.getMaxDevices(), at.getMaxConnections());
    }
}
