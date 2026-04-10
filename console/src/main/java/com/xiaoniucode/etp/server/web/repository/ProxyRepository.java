package com.xiaoniucode.etp.server.web.repository;

import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.server.web.entity.ProxyDO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 代理 Repository
 */
@Repository
public interface ProxyRepository extends JpaRepository<ProxyDO, String> {
    boolean existsByAgentIdAndName(String agentId, String name);

    boolean existsByAgentIdAndNameAndIdNot(String agentId, String name, String id);

    @Query("""
                SELECT p, a
                FROM ProxyDO p
                LEFT JOIN AgentDO a ON a.id = p.agentId
                WHERE (:keyword IS NULL OR
                       p.name LIKE :keyword OR
                       p.agentId LIKE :keyword)
                  AND p.protocol = :protocolType
                ORDER BY p.updatedAt DESC
            """)
    Page<Object[]> findProxiesWithAssociations(
            @Param("keyword") String keyword,
            @Param("protocolType") ProtocolType protocolType,
            Pageable pageable
    );

    @Query("""
            SELECT a, t, b, lb
            FROM ProxyDO p
            LEFT JOIN AgentDO a ON p.agentId = a.id
            LEFT JOIN TransportDO t ON t.proxyId = p.id
            LEFT JOIN BandwidthDO b ON b.proxyId = p.id
            LEFT JOIN LoadBalanceDO lb ON lb.proxyId = p.id
            WHERE p.id = :id
            """)
    Optional<Object[]> findProxyDetailWithAssociations(@Param("id") String id);

    @Query("""
            SELECT a, t, b, lb
            FROM ProxyDO p
            LEFT JOIN AgentDO a ON p.agentId = a.id
            LEFT JOIN TransportDO t ON t.proxyId = p.id
            LEFT JOIN BandwidthDO b ON b.proxyId = p.id
            LEFT JOIN LoadBalanceDO lb ON lb.proxyId = p.id
            WHERE p.id = :id
            """)
    void deleteByIdIn(List<String> ids);
}
