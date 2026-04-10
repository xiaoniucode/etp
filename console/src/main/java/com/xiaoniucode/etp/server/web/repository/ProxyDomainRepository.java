package com.xiaoniucode.etp.server.web.repository;
import com.xiaoniucode.etp.server.web.entity.HttpProxyDomainDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 代理域名 Repository
 */
@Repository
public interface ProxyDomainRepository extends JpaRepository<HttpProxyDomainDO, Integer> {
    void deleteByProxyId(String proxyId);

    List<HttpProxyDomainDO> findByProxyIdIn(List<String> proxyIds);

    List<HttpProxyDomainDO> findByProxyId(String id);

    void deleteByProxyIdIn(List<String> ids);
}
