package com.xiaoniucode.etp.server.web.repository;

import com.xiaoniucode.etp.server.web.domain.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 客户端 Repository
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, Integer> {
    
    /**
     * 根据名称查询客户端
     */
    Client findByName(String name);
    
    /**
     * 根据密钥查询客户端
     */
    Client findByToken(String token);
}
