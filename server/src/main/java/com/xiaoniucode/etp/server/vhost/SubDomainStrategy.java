package com.xiaoniucode.etp.server.vhost;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
@Component
public class SubDomainStrategy implements DomainGenerationStrategy {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(SubDomainStrategy.class);
    
    @Override
    public boolean supports(ProxyConfig config) {
        Set<String> subDomains = config.getDomainInfo().getSubDomains();
        return subDomains != null && !subDomains.isEmpty();
    }
    
    @Override
    public Set<DomainInfo> generate(ProxyConfig config, DomainManager domainManager, String[] baseDomains) {
        if (baseDomains.length == 0) {
            return Collections.emptySet();
        }
        
        Set<DomainInfo> result = new HashSet<>();
        
        for (String prefix : config.getDomainInfo().getSubDomains()) {
            if (prefix == null || prefix.trim().isEmpty()) {
                continue;
            }
            
            // 随机选择一个基础域名
            String baseDomain = baseDomains[ThreadLocalRandom.current().nextInt(baseDomains.length)];
            SubDomainInfo subDomain = new SubDomainInfo(baseDomain, prefix);
            
            if (domainManager.exists(subDomain.getFullDomain())) {
                logger.warn("子域名已被占用：{}", subDomain.getFullDomain());
                continue;
            }
            
            result.add(subDomain);
            logger.debug("生成子域名成功：{}", subDomain.getFullDomain());
        }
        
        return result;
    }

    @Override
    public int getOrder() {
        return 2;
    }
}