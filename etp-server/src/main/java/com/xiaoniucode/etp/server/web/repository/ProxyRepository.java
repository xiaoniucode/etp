package com.xiaoniucode.etp.server.web.repository;

import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.server.web.entity.Proxy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 代理 Repository
 */
@Repository
public interface ProxyRepository extends JpaRepository<Proxy, String> {
    
    /**
     * 根据客户端ID查询代理
     */
    List<Proxy> findByClientId(String clientId);
    
    /**
     * 根据名称查询代理
     */
    Proxy findByName(String name);
    
    /**
     * 根据类型查询代理
     */
    List<Proxy> findByProtocol(ProtocolType protocol);
    
    /**
     * 根据状态查询代理
     */
    List<Proxy> findByStatus(Integer status);
}
