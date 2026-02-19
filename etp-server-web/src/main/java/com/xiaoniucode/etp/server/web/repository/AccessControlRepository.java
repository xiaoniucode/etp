package com.xiaoniucode.etp.server.web.repository;

import com.xiaoniucode.etp.server.web.entity.AccessControl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 访问控制 Repository
 */
@Repository
public interface AccessControlRepository extends JpaRepository<AccessControl, Integer> {
    
    /**
     * 根据代理ID查询访问控制
     */
    AccessControl findByProxyId(String proxyId);
}
