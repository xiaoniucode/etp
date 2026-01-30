package com.xiaoniucode.etp.server.web.repository;

import com.xiaoniucode.etp.server.web.domain.Proxy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 代理 Repository
 */
@Repository
public interface ProxyRepository extends JpaRepository<Proxy, Integer> {
    
    /**
     * 根据客户端ID查询代理
     */
    List<Proxy> findByClientId(Integer clientId);
    
    /**
     * 根据名称查询代理
     */
    Proxy findByName(String name);
    
    /**
     * 根据类型查询代理
     */
    List<Proxy> findByType(String type);
    
    /**
     * 根据状态查询代理
     */
    List<Proxy> findByStatus(Integer status);
}
