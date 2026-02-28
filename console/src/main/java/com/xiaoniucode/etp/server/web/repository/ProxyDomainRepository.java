package com.xiaoniucode.etp.server.web.repository;

import com.xiaoniucode.etp.server.web.entity.ProxyDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * 代理域名 Repository
 */
@Repository
public interface ProxyDomainRepository extends JpaRepository<ProxyDomain, Integer> {
    
    /**
     * 根据代理ID查询域名
     */
    List<ProxyDomain> findByProxyId(String proxyId);
    
    /**
     * 根据域名查询
     */
    ProxyDomain findByDomain(String domain);
    
    /**
     * 根据代理ID列表删除域名
     */
    void deleteByProxyIdIn(Set<String> proxyIds);
}
