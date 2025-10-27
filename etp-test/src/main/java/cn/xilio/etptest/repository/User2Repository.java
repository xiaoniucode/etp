package cn.xilio.etptest.repository;

import cn.xilio.etptest.entity.User2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author liuxin
 */
@Repository
public interface User2Repository extends JpaRepository<User2, String> {
}
