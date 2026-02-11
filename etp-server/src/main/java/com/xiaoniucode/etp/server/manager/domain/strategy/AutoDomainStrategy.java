package com.xiaoniucode.etp.server.manager.domain.strategy;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.server.manager.DomainManager;
import com.xiaoniucode.etp.server.manager.domain.AutoDomainInfo;
import com.xiaoniucode.etp.server.manager.domain.DomainInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class AutoDomainStrategy implements DomainGenerationStrategy {
    private static final Logger logger = LoggerFactory.getLogger(AutoDomainStrategy.class);
    private static final int MIN_PREFIX_LENGTH = 2;
    private static final int MAX_PREFIX_LENGTH = 10;
    private static final String PREFIX_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";
    
    @Override
    public boolean supports(ProxyConfig config) {
        return Boolean.TRUE.equals(config.getAutoDomain());
    }
    
    @Override
    public Set<DomainInfo> generate(ProxyConfig config, DomainManager domainManager, String[] baseDomains) {
        if (baseDomains.length == 0) {
            return Collections.emptySet();
        }
        
        Set<DomainInfo> result = new HashSet<>();
        
        // 随机生成前缀
        String prefix = generateRandomPrefix();
        // 随机选择基础域名
        String baseDomain = baseDomains[ThreadLocalRandom.current().nextInt(baseDomains.length)];
        
        AutoDomainInfo autoDomain = new AutoDomainInfo(baseDomain, prefix);
        
        if (!domainManager.exists(autoDomain.getFullDomain())) {
            result.add(autoDomain);
            logger.debug("自动生成域名成功：{}", autoDomain.getFullDomain());
        } else {
            logger.warn("自动生成的域名已被占用：{}", autoDomain.getFullDomain());
            //todo 重试
        }
        
        return result;
    }
    
    private String generateRandomPrefix() {
        int length = ThreadLocalRandom.current().nextInt(MIN_PREFIX_LENGTH, MAX_PREFIX_LENGTH + 1);
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(PREFIX_CHARS.charAt(ThreadLocalRandom.current().nextInt(PREFIX_CHARS.length())));
        }
        return sb.toString();
    }

    @Override
    public int getOrder() {
        return 3;
    }
}