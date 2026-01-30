package com.xiaoniucode.etp.server.web.repository;

import com.xiaoniucode.etp.server.web.domain.LoginToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 认证令牌 Repository
 */
@Repository
public interface LoginTokenRepository extends JpaRepository<LoginToken, String> {
}
