package cn.xilio.etptest.repository;

import cn.xilio.etptest.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author liuxin
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {
}
