/*
 *    Copyright 2026 xiaoniucode
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.xiaoniucode.etp.server.web.listener;

import com.xiaoniucode.etp.common.utils.StringUtils;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.config.domain.DashboardConfig;
import com.xiaoniucode.etp.server.web.entity.SysUserDO;
import com.xiaoniucode.etp.server.web.repository.UserRepository;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 初始化管理员用户，首次从配置文件中加载，后续修改以数据库为主
 */
@Component
public class AdminInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminInitializer.class);

    @Resource
    private AppConfig appConfig;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    /**
     * 默认角色
     */
    private final static String DEFAULT_ROLE = "R_SUPER";

    @Override
    public void run(ApplicationArguments args) {
        try {
            DashboardConfig dashboard = appConfig.getDashboard();
            if (dashboard == null) {
                return;
            }
            String username = dashboard.getUsername();
            String password = dashboard.getPassword();

            Optional<SysUserDO> existOpt = userRepository.findByUsername(username);
            if (existOpt.isPresent()) {
                logger.debug("管理员用户已存在于数据库中，跳过初始化");
                return;
            }
            if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
                logger.warn("Dashboard 管理员用户名或密码未配置，跳过管理员初始化");
                return;
            }
            SysUserDO sysUserDO = new SysUserDO();
            sysUserDO.setUsername(username);
            sysUserDO.setPassword(passwordEncoder.encode(password));
            sysUserDO.setRole(DEFAULT_ROLE);
            userRepository.save(sysUserDO);
            logger.debug("管理员用户 {} 已成功初始化", username);
        } catch (Exception e) {
            logger.error("初始化管理员用户失败", e);
            throw e;
        }
    }
}