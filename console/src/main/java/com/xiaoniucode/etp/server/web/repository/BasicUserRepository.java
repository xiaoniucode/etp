package com.xiaoniucode.etp.server.web.repository;
import com.xiaoniucode.etp.server.web.entity.BasicUserDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * HttpUser Repository
 */
@Repository
public interface BasicUserRepository extends JpaRepository<BasicUserDO, Integer> {
    void deleteByProxyId(String id);

    void deleteByProxyIdIn(List<String> ids);
}
