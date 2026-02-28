package com.xiaoniucode.etp.server.web.repository;

import com.xiaoniucode.etp.server.web.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 客户端 Repository
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, String> {
}
