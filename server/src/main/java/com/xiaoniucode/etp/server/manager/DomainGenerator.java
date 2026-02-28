package com.xiaoniucode.etp.server.manager;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.server.manager.domain.DomainInfo;
import com.xiaoniucode.etp.server.manager.domain.strategy.DomainGenerationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class DomainGenerator {
    private final Logger logger = LoggerFactory.getLogger(DomainGenerator.class);
    private final String[] baseDomains;
    private final DomainManager domainManager;
    private final List<DomainGenerationStrategy> strategies;

    public DomainGenerator(Set<String> baseDomains, DomainManager domainManager, List<DomainGenerationStrategy> strategies) {
        this.baseDomains = baseDomains.toArray(new String[0]);
        this.domainManager = domainManager;
        // 按优先级排序
        this.strategies = strategies.stream()
                .sorted(Comparator.comparingInt(DomainGenerationStrategy::getOrder))
                .collect(Collectors.toList());
        logger.debug("基础域名数组已刷新，长度：{}", this.baseDomains.length);
        logger.debug("已加载域名生成策略：{}",
                this.strategies.stream().map(s -> s.getClass().getSimpleName()).collect(Collectors.joining(", ")));
    }

    /**
     * 优先级：自定义域名--子域名--自动生成域名
     *
     * @param config 代理配置信息
     * @return 可用域名列表
     */
    public Set<DomainInfo> generate(ProxyConfig config) {
        for (DomainGenerationStrategy strategy : strategies) {
            if (strategy.supports(config)) {
                Set<DomainInfo> domains = strategy.generate(config, domainManager, baseDomains);
                if (!domains.isEmpty()) {
                    return domains;  // 命中第一个支持的策略并返回结果
                }
            }
        }
        return Collections.emptySet();
    }
}
