package com.xiaoniucode.etp.server.web.repository;
import com.xiaoniucode.etp.server.web.entity.LoginTokenDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
/**
 * 认证令牌 Repository
 */
@Repository
public interface LoginTokenRepository extends JpaRepository<LoginTokenDO, String> {
}
