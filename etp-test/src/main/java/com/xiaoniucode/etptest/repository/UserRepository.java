package com.xiaoniucode.etptest.repository;

import com.xiaoniucode.etptest.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author liuxin
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {
}
