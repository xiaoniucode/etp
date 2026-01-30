package com.xiaoniucode.etp.server.web.repository;

import com.xiaoniucode.etp.server.web.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 用户 Repository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    
    /**
     * 根据用户名查询用户
     */
    User findByUsername(String username);
}
