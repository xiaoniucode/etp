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

package com.xiaoniucode.etp.server.vhost;

import com.xiaoniucode.etp.core.domain.RouteConfig;
import com.xiaoniucode.etp.core.enums.DomainType;
import com.xiaoniucode.etp.server.exceptions.DomainConflictException;
import com.xiaoniucode.etp.server.exceptions.EtpException;
import com.xiaoniucode.etp.server.service.DomainConfigService;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class DomainGenerator {
    private final InternalLogger logger = InternalLoggerFactory.getInstance(DomainGenerator.class);

    private static final int MIN_PREFIX_LENGTH = 2;
    private static final int MAX_PREFIX_LENGTH = 10;
    private static final String PREFIX_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";
    @Autowired
    private DomainConfigService domainConfigService;

    /**
     * 子域名生成
     *
     * @param baseDomain  基础域名
     * @param routeConfig 域名配置信息
     * @return 生成的域名绑定列表
     */
    public List<String> generateSubdomain(String baseDomain, RouteConfig routeConfig) throws DomainConflictException {
        DomainType domainType = routeConfig.getDomainType();
        List<String> res = new ArrayList<>();
        if (domainType == DomainType.CUSTOM_DOMAIN || DomainType.SUBDOMAIN == domainType) {
            throw new EtpException("不支持自定义域名生成，请直接绑定域名");
        }
        String prefix = generateRandomDomainPrefix(baseDomain);
        res.add(prefix);
        return res;
    }

    private String generateRandomDomainPrefix(String baseDomain) {
        for (int i = 0; i < 20; i++) {
            String prefix = generateRandomPrefix();
            if (!domainConfigService.existsSubdomain(baseDomain, prefix)) {
                return prefix;
            }
        }
        return null;
    }

    private String generateRandomPrefix() {
        int length = ThreadLocalRandom.current().nextInt(MIN_PREFIX_LENGTH, MAX_PREFIX_LENGTH + 1);
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(PREFIX_CHARS.charAt(ThreadLocalRandom.current().nextInt(PREFIX_CHARS.length())));
        }
        return sb.toString();
    }
}
