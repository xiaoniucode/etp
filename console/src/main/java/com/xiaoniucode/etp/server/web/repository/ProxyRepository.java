package com.xiaoniucode.etp.server.web.repository;
import com.xiaoniucode.etp.server.web.entity.ProxyDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
/**
 * 代理 Repository
 */
@Repository
public interface ProxyRepository extends JpaRepository<ProxyDO, String> {
}
