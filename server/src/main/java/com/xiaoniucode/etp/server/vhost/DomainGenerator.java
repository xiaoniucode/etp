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
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import lombok.AllArgsConstructor;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

@AllArgsConstructor
public class DomainGenerator {
    private final InternalLogger logger = InternalLoggerFactory.getInstance(DomainGenerator.class);

    private final String baseDomain;
    private static final int MIN_PREFIX_LENGTH = 2;
    private static final int MAX_PREFIX_LENGTH = 10;
    private static final String PREFIX_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";

    public List<DomainBinding> generate(String proxyId, RouteConfig routeConfig, Function<String, Boolean> occupiedChecker) {
        DomainType domainType = routeConfig.getDomainType();
        List<DomainBinding> res = new ArrayList<>();
        if (domainType == DomainType.CUSTOM_DOMAIN) {
            Set<String> domains = routeConfig.getCustomDomains();
            for (String domain : domains) {
                if (occupiedChecker.apply(domain)) {
                    throw new DomainConflictException("域名[" + domain + "]已被占用");
                } else {
                    DomainBinding domainBinding = new DomainBinding(proxyId, null, domain, routeConfig.getDomainType());
                    res.add(domainBinding);
                }
            }
        }
        if (domainType == DomainType.SUBDOMAIN) {
            Set<String> subDomains = routeConfig.getSubDomains();
            for (String prefix : subDomains) {
                String fullDomain = prefix + "." + baseDomain;
                if (occupiedChecker.apply(fullDomain)) {
                    throw new DomainConflictException("子域名[" + prefix + "]已被占用");
                } else {
                    DomainBinding domainBinding = new DomainBinding(proxyId, baseDomain, prefix, routeConfig.getDomainType());
                    res.add(domainBinding);
                }
            }
        }
        if (domainType == DomainType.AUTO) {
            String prefix = generateRandomDomainPrefix(occupiedChecker);
            DomainBinding domainBinding = new DomainBinding(proxyId, baseDomain, prefix, routeConfig.getDomainType());
            res.add(domainBinding);
        }
        return res;
    }

    private String generateRandomDomainPrefix(Function<String, Boolean> occupiedChecker) {
        //最多重试20次
        for (int i = 0; i < 20; i++) {
            String prefix = generateRandomPrefix();
            if (!occupiedChecker.apply(prefix + "." + baseDomain)) {
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
