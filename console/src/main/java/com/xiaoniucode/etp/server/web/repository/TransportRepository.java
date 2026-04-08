package com.xiaoniucode.etp.server.web.repository;

import com.xiaoniucode.etp.server.web.entity.TransportDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 传输配置 Repository
 */
@Repository
public interface TransportRepository extends JpaRepository<TransportDO, String> {
}
