package com.xiaoniucode.etp.server.web.listener;

import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.core.notify.EventListener;
import com.xiaoniucode.etp.server.config.domain.*;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.event.TunnelServerBindEvent;
import com.xiaoniucode.etp.server.web.repository.AccessTokenRepository;
import com.xiaoniucode.etp.server.web.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据同步，如果开启了管理界面，则将静态配置同步到数据库
 */
@Component
public class StaticConfigSync implements EventListener<TunnelServerBindEvent> {
    private static final Logger logger = LoggerFactory.getLogger(StaticConfigSync.class);
    @Autowired
    private EventBus eventBus;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccessTokenRepository accessTokenRepository;
    @Resource
    private AppConfig config;

    @PostConstruct
    public void init() {
        eventBus.register(this);
    }

    @Override
    public void onEvent(TunnelServerBindEvent event) {
        try {
            if (config.getDashboard().getEnabled()) {
//                syncDashboardUser();
//                syncAccessTokens();
            } else {
                logger.debug("管理面板未启用，跳过数据同步");
            }
        } catch (Exception e) {
            logger.error("数据同步失败", e);
            throw e; // 事务会回滚
        }
    }

//    public void syncDashboardUser() {
//        DashboardConfig dashboard = config.getDashboard();
//        Boolean reset = dashboard.getReset();
//        String username = dashboard.getUsername();
//        String password = dashboard.getPassword();
//        //没有直接添加用户
//        if (userRepository.findByUsername(username) == null) {
//
//            userRepository.saveAndFlush(new SysUser(username, passwordEncoder.encode(password)));
//            logger.debug("同步用户: {} 配置到数据库", username);
//        } else if (reset) {
//            //删除所有用户
//            userRepository.deleteAll();
//            //重新注册
//            userRepository.saveAndFlush(new SysUser(username, passwordEncoder.encode(password)));
//            logger.debug("重置管理面板认证信息");
//        } else {
//            logger.debug("管理面板登录：使用配置文件中的用户凭证");
//        }
//    }
//
//    public void syncAccessTokens() {
//        AuthConfig authConfig = config.getAuthConfig();
//        List<TokenConfig> configTokens = authConfig.getTokens();
//        if (configTokens == null || configTokens.isEmpty()) {
//            return;
//        }
//
//        // 提取 token 列表
//        List<String> tokenValues = configTokens.stream()
//                .map(TokenConfig::getToken)
//                .collect(Collectors.toList());
//
//        // 批量查询已存在的 token
//        Set<String> existingTokenValues = accessTokenRepository.findByTokenIn(tokenValues)
//                .stream()
//                .map(AccessToken::getToken)
//                .collect(Collectors.toSet());
//
//        // 过滤并创建新 token
//        List<AccessToken> tokensToSave = configTokens.stream()
//                .filter(token -> !existingTokenValues.contains(token.getToken()))
//                .map(token -> {
//                    logger.debug("准备同步客户端访问令牌：[名称={}，令牌={}]",
//                            token.getName(), token.getToken());
//                    return new AccessToken(
//                            token.getName(),
//                            token.getToken(),
//                            token.getMaxClients()
//                    );
//                })
//                .collect(Collectors.toList());
//
//        // 记录警告
//        configTokens.stream()
//                .filter(token -> existingTokenValues.contains(token.getToken()))
//                .forEach(token ->
//                        logger.warn("数据库已存在相同客户端访问令牌，跳过同步: [名称={}，令牌={}]",
//                                token.getName(), token.getToken())
//                );
//
//        // 批量插入
//        if (!tokensToSave.isEmpty()) {
//            List<AccessToken> savedTokens = accessTokenRepository.saveAll(tokensToSave);
//            logger.info("批量同步了 {} 个客户端访问令牌", savedTokens.size());
//        }
//    }
}
