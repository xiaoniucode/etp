package com.xiaoniucode.etp.server.web.repository;

import com.xiaoniucode.etp.server.web.domain.Config;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 系统配置 Repository
 */
@Repository
public interface ConfigRepository extends JpaRepository<Config, Integer> {
    
    /**
     * 根据 configKey 查询配置
     */
    Config findByConfigKey(String configKey);
}
