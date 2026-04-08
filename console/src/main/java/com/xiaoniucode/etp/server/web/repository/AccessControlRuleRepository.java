package com.xiaoniucode.etp.server.web.repository;
import com.xiaoniucode.etp.server.web.entity.AccessControlRuleDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
/**
 * 访问控制规则 Repository
 */
@Repository
public interface AccessControlRuleRepository extends JpaRepository<AccessControlRuleDO, Integer> {
}
