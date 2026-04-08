package com.xiaoniucode.etp.server.web.repository;
import com.xiaoniucode.etp.server.web.entity.BasicUserDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
/**
 * HttpUser Repository
 */
@Repository
public interface HttpUserRepository extends JpaRepository<BasicUserDO, Integer> {
}
