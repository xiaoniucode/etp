package com.xiaoniucode.etp.server.web.repository;
import com.xiaoniucode.etp.server.web.entity.HttpProxyDomainDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
/**
 * 代理域名 Repository
 */
@Repository
public interface ProxyDomainRepository extends JpaRepository<HttpProxyDomainDO, Integer> {
}
