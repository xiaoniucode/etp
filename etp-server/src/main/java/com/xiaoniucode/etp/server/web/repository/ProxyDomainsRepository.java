package com.xiaoniucode.etp.server.web.repository;

import com.xiaoniucode.etp.server.web.domain.ProxyDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 代理域名 Repository
 */
@Repository
public interface ProxyDomainsRepository extends JpaRepository<ProxyDomain, Integer> {
    
    /**
     * 根据代理ID查询域名
     */
    List<ProxyDomain> findByProxyId(Integer proxyId);
    
    /**
     * 根据域名查询
     */
    ProxyDomain findByDomain(String domain);
}
