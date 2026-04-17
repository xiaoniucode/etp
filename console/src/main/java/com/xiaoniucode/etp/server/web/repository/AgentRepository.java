package com.xiaoniucode.etp.server.web.repository;

import com.xiaoniucode.etp.server.web.entity.AgentDO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 客户端 Repository
 */
@Repository
public interface AgentRepository extends JpaRepository<AgentDO, String> {
    /**
     * 根据关键词搜索客户端
     */
    @Query("SELECT a FROM AgentDO a WHERE a.name LIKE %:keyword% OR a.id LIKE %:keyword%")
    List<AgentDO> findByKeyword(@Param("keyword") String keyword);

    /**
     * 根据关键词搜索客户端
     */
    @Query("SELECT a FROM AgentDO a WHERE a.name LIKE %:keyword% OR a.id LIKE %:keyword%")
    Page<AgentDO> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    List<AgentDO> findByIdIn(List<String> agentIds);

    /**
     * 根据 token 查询代理
     */
    List<AgentDO> findByToken(String token);

    /**
     * 根据 token 统计代理数量
     */
    long countByToken(String token);

    /**
     * 根据 token 查询过期的离线代理
     */
    @Query("SELECT a FROM AgentDO a WHERE a.token = :token AND a.lastActiveTime < :expireTime")
    List<AgentDO> findExpiredByToken(@Param("token") String token, @Param("expireTime") LocalDateTime expireTime);
}
