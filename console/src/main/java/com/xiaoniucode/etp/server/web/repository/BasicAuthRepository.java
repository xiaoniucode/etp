package com.xiaoniucode.etp.server.web.repository;
import com.xiaoniucode.etp.server.web.entity.BasicAuthDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
/**
 * BasicAuth Repository
 */
@Repository
public interface BasicAuthRepository extends JpaRepository<BasicAuthDO, String> {
}
