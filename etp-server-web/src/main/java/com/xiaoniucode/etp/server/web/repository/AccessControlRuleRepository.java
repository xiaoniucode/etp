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
    
    /**
     * 根据访问控制ID查询规则列表
     */
    List<AccessControlRule> findByAcId(Integer acId);
    
    /**
     * 根据访问控制ID删除规则
     */
    void deleteByAcId(Integer acId);
}
