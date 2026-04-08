package com.xiaoniucode.etp.server.web.repository;
import com.xiaoniucode.etp.server.web.entity.AccessTokenDO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
/**
 * 访问令牌 Repository
 */
@Repository
public interface AccessTokenRepository extends JpaRepository<AccessTokenDO, Integer> {
    /**
     * 根据关键词搜索访问令牌（分页）
     */
    @Query("SELECT a FROM AccessTokenDO a WHERE a.name LIKE %:keyword% OR a.token LIKE %:keyword%")
    Page<AccessTokenDO> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
