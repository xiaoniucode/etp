package com.xiaoniucode.etp.server.web.repository;

import com.xiaoniucode.etp.server.web.entity.ProxyDO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 代理 Repository
 */
@Repository
public interface ProxyRepository extends JpaRepository<ProxyDO, String> {
    boolean existsByAgentIdAndName(String agentId, String name);

    boolean existsByAgentIdAndNameAndIdNot(String agentId, String name, String id);

    @Query("""
            SELECT p 
            FROM ProxyDO p 
            WHERE (:keyword IS NULL OR :keyword = '' OR p.name LIKE CONCAT('%', :keyword, '%'))
            ORDER BY p.updatedAt DESC
            """)
    Page<ProxyDO> findHttpProxiesByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
