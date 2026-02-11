package com.xiaoniucode.etp.server.manager.domain.strategy;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.server.manager.DomainManager;
import com.xiaoniucode.etp.server.manager.domain.CustomDomainInfo;
import com.xiaoniucode.etp.server.manager.domain.DomainInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class CustomDomainStrategy implements DomainGenerationStrategy {
    private static final Logger logger = LoggerFactory.getLogger(CustomDomainStrategy.class);

    @Override
    public boolean supports(ProxyConfig config) {
        Set<String> customDomains = config.getCustomDomains();
        return customDomains != null && !customDomains.isEmpty();
    }

    @Override
    public Set<DomainInfo> generate(ProxyConfig config, DomainManager domainManager, String[] baseDomains) {
        Set<DomainInfo> result = new HashSet<>();

        for (String domain : config.getCustomDomains()) {
            CustomDomainInfo customDomain = new CustomDomainInfo(domain);

            if (!customDomain.isAvailable()) {
                logger.warn("自定义域名格式无效：{}", domain);
                continue;
            }

            if (domainManager.exists(customDomain.getFullDomain())) {
                logger.warn("自定义域名已被占用：{}", customDomain.getFullDomain());
                continue;
            }

            result.add(customDomain);
            logger.debug("生成自定义域名成功：{}", customDomain.getFullDomain());
        }

        return result;
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
