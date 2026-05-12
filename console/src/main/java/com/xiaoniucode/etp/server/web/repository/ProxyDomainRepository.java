package com.xiaoniucode.etp.server.web.repository;

import com.xiaoniucode.etp.server.web.entity.ProxyDomainDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 代理域名 Repository
 */
@Repository
public interface ProxyDomainRepository extends JpaRepository<ProxyDomainDO, Integer> {
    void deleteByProxyId(String proxyId);

    List<ProxyDomainDO> findByProxyIdIn(List<String> proxyIds);

    List<ProxyDomainDO> findByProxyId(String id);

    void deleteByProxyIdIn(List<String> ids);

    Optional<ProxyDomainDO> findByDomainAndBaseDomain(String domain, String baseDomain);

    Optional<ProxyDomainDO> findByDomainAndBaseDomainIsNull(String fullDomain);

    Optional<ProxyDomainDO> findByDomain(String domain);

    boolean existsByDomainAndBaseDomain(String domain, String baseDomain);

    boolean existsByFullDomain(String fullDomain);

    List<ProxyDomainDO> findByFullDomainIn(Collection<String> fullDomains);

    Optional<ProxyDomainDO> findByFullDomain(String fullDomain);

    @Query("SELECT p.fullDomain FROM ProxyDomainDO p WHERE p.proxyId = :proxyId")
    Set<String> findFullDomainsByProxyId(@Param("proxyId") String proxyId);
}
