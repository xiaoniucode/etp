package com.xiaoniucode.etp.server.web.repository;

import com.xiaoniucode.etp.server.web.entity.AccessControlRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 访问控制规则 Repository
 */
@Repository
public interface AccessControlRuleRepository extends JpaRepository<AccessControlRule, Integer> {
    List<AccessControlRule> findByProxyId(String acId);
    
    void deleteByProxyId(String proxyId);
}
