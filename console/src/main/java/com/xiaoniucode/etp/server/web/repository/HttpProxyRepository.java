package com.xiaoniucode.etp.server.web.repository;

import com.xiaoniucode.etp.server.web.entity.HttpProxyDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * TCP代理 Repository
 */
@Repository
public interface HttpProxyRepository extends JpaRepository<HttpProxyDO, String> {
}
