package com.xiaoniucode.etp.server.manager.temp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DomainGenerator {
    private final Set<String> baseDomains = ConcurrentHashMap.newKeySet();

    @Autowired
    private DomainManager domainManager;

    /**
     * 初始化基础域名
     *
     * @param baseDomains 域名列表
     */
    public void setBaseDomains(Set<String> baseDomains) {
        this.baseDomains.clear();
        this.baseDomains.addAll(baseDomains);
    }

}
