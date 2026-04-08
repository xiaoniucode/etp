package com.xiaoniucode.etp.server.web.repository;

import com.xiaoniucode.etp.server.web.entity.TcpProxyDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * TCP代理 Repository
 */
@Repository
public interface TcpProxyRepository extends JpaRepository<TcpProxyDO, String> {
}
