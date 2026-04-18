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

import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.store.DomainStore;
import com.xiaoniucode.etp.server.vhost.DomainBinding;
import com.xiaoniucode.etp.server.web.entity.HttpProxyDomainDO;
import com.xiaoniucode.etp.server.web.repository.ProxyDomainRepository;
import com.xiaoniucode.etp.server.web.support.store.converter.DomainStoreConvert;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Primary
public class CompositeDomainStore implements DomainStore {
    private final Logger logger = LoggerFactory.getLogger(CompositeDomainStore.class);

    private final String CACHE_NAME = "vhost_domain";
    @Autowired
    private MultiLevelCache multiLevelCache;
    @Autowired
    private ProxyDomainRepository proxyDomainRepository;
    @Resource
    private AppConfig appConfig;
    @Autowired
    private DomainStoreConvert domainStoreConvert;

    @Override
    public boolean isOccupied(String fullDomain) {
        boolean result = findByDomain(fullDomain) != null;
        logger.debug("域名占用状态检查完成，完整域名: {}, 占用状态: {}", fullDomain, result);
        return result;
    }

    @Override
    public void save(DomainBinding domainBinding) {
        clearDomainCache(domainBinding.getProxyId(), domainBinding.getFullDomain());
        logger.debug("域名绑定保存完成，代理ID: {}, 完整域名: {}", domainBinding.getProxyId(), domainBinding.getFullDomain());
    }

    @Override
    public DomainBinding findByDomain(String fullDomain) {
        if (fullDomain == null || fullDomain.isBlank()) {
            logger.debug("完整域名为空，返回null");
            return null;
        }
        
        String cacheKey = "domain:" + fullDomain;
        DomainBinding result = multiLevelCache.getAndPut(CACHE_NAME, cacheKey, () -> {
            String baseDomain = appConfig.getBaseDomain();
            boolean isSubDomain = fullDomain.endsWith("." + baseDomain);
            logger.debug("完整域名: {}, 基础域名: {}, 是否为子域名: {}", fullDomain, baseDomain, isSubDomain);
            
            HttpProxyDomainDO domainDO;
            if (isSubDomain) {
                String domain = fullDomain.substring(0, fullDomain.length() - baseDomain.length() - 1);
                logger.debug("子域名前缀: {}", domain);
                domainDO = proxyDomainRepository.findByDomainAndBaseDomain(domain, baseDomain).orElse(null);
            } else {
                domainDO = proxyDomainRepository.findByDomainAndBaseDomainIsNull(fullDomain).orElse(null);
            }
            
            return domainDO != null ? domainStoreConvert.toDomainBinding(domainDO) : null;
        });
        logger.debug("根据完整域名查询绑定完成，完整域名: {}, 结果: {}", fullDomain, result != null ? "找到绑定" : "未找到绑定");
        return result;
    }

    @Override
    public List<DomainBinding> findByProxyId(String proxyId) {
        String cacheKey = "proxy:" + proxyId;
        List<DomainBinding> result = multiLevelCache.getAndPut(CACHE_NAME, cacheKey, () -> {
            List<HttpProxyDomainDO> domainDOs = proxyDomainRepository.findByProxyId(proxyId);
            logger.debug("查询到代理ID: {} 的域名绑定数量: {}", proxyId, domainDOs.size());
            return domainStoreConvert.toDomainBindingList(domainDOs);
        });
        logger.debug("根据代理ID查询域名绑定完成，代理ID: {}, 结果数量: {}", proxyId, result.size());
        return result;
    }

    @Override
    public void delete(String proxyId) {
        clearDomainCache(proxyId, null);
        logger.debug("删除代理的所有域名绑定完成，代理ID: {}", proxyId);
    }

    @Override
    public void delete(String proxyId, String fullDomain) {
        clearDomainCache(proxyId, fullDomain);
        logger.debug("删除指定域名绑定完成，代理ID: {}, 完整域名: {}", proxyId, fullDomain);
    }

    private void clearDomainCache(String proxyId, String fullDomain) {
        multiLevelCache.evict(CACHE_NAME, "proxy:" + proxyId);
        if (fullDomain != null) {
            multiLevelCache.evict(CACHE_NAME, "domain:" + fullDomain);
            multiLevelCache.evict(CACHE_NAME, "occupied:" + fullDomain);
        }
    }
}
