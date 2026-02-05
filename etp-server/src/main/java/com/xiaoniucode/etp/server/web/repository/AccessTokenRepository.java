package com.xiaoniucode.etp.server.web.repository;

import com.xiaoniucode.etp.server.web.domain.AccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 访问令牌 Repository
 */
@Repository
public interface AccessTokenRepository extends JpaRepository<AccessToken, Integer> {
    
    AccessToken findByToken(String token);

    List<AccessToken> findByTokenIn(List<String> tokens);
}