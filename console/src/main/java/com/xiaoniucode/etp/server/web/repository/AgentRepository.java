package com.xiaoniucode.etp.server.web.repository;

import com.xiaoniucode.etp.server.web.entity.Agent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 客户端 Repository
 */
@Repository
public interface AgentRepository extends JpaRepository<Agent, String> {
    /**
     * 根据关键词搜索客户端
     */
    @Query("SELECT a FROM Agent a WHERE a.name LIKE %:keyword% OR a.id LIKE %:keyword%")
    List<Agent> findByKeyword(@Param("keyword") String keyword);
    
    /**
     * 根据关键词搜索客户端（分页）
     */
    @Query("SELECT a FROM Agent a WHERE a.name LIKE %:keyword% OR a.id LIKE %:keyword%")
    Page<Agent> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
}

