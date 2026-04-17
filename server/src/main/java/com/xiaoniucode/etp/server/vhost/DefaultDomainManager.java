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
import com.xiaoniucode.etp.server.exceptions.EtpException;
import com.xiaoniucode.etp.server.store.DomainStore;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class DefaultDomainManager implements DomainManager {
    private final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultDomainManager.class);
    private final DomainGenerator domainGenerator;
    private final DomainStore domainStore;

    @Override
    public synchronized List<DomainBinding> register(String proxyId, RouteConfig routeConfig) throws EtpException {
        List<DomainBinding> domains = domainGenerator.generate(proxyId, routeConfig, domainStore::isOccupied);
        if (domains.isEmpty()) {
            throw new EtpException("无法为代理[" + proxyId + "]生成域名");
        }
        for (DomainBinding domainBinding : domains) {
            domainStore.save(domainBinding);
        }
        logger.debug("为代理[{}]注册域名: {}", proxyId, domains);
        return domains;
    }

    @Override
    public synchronized DomainBinding register(String proxyId, String domain, DomainType domainType) throws EtpException {
        if (domainType == DomainType.AUTO) {
            throw new EtpException("域名类型不能为 AUTO");
        }
        DomainBinding domainBinding = domainGenerator.generate(proxyId, domain, domainType, domainStore::isOccupied);
        domainStore.save(domainBinding);
        logger.debug("为代理[{}]注册域名: {}", proxyId, domainBinding);
        return domainBinding;
    }

    @Override
    public void unregister(String proxyId, String domain) {
        domainStore.delete(proxyId, domain);
    }

    @Override
    public void unregister(String proxyId, List<String> domains) {
        for (String domain : domains) {
            domainStore.delete(proxyId, domain);
        }
    }

    @Override
    public void unregister(String proxyId) {
        domainStore.delete(proxyId);
    }

    @Override
    public List<DomainBinding> getBoundDomains(String proxyId) {
        return domainStore.findByProxyId(proxyId);
    }

    @Override
    public Optional<DomainBinding> getDomainBinding(String domain) {
        return Optional.ofNullable(domainStore.findByDomain(domain));
    }

    @Override
    public DomainBinding match(String domain) {
        return domainStore.findByDomain(domain);
    }

    @Override
    public boolean exist(String domain) {
        return match(domain) != null;
    }
}
