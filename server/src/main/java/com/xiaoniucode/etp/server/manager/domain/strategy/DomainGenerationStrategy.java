package com.xiaoniucode.etp.server.manager.domain.strategy;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.server.manager.DomainManager;
import com.xiaoniucode.etp.server.manager.domain.DomainInfo;

import java.util.Set;

public interface DomainGenerationStrategy {
    /**
     * 判断是否支持当前配置
     */
    boolean supports(ProxyConfig config);

    /**
     * 生成域名
     *
     * @return 生成的域名集合，可能为空
     */
    Set<DomainInfo> generate(ProxyConfig config, DomainManager domainManager, String[] baseDomains);
    /**
     * 策略优先级（数值越小优先级越高）
     */
    default int getOrder() {
        return 0;
    }
}
