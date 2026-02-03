package com.xiaoniucode.etp.server.web.listener;

import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.core.notify.EventListener;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.config.domain.AccessTokenInfo;
import com.xiaoniucode.etp.server.event.TunnelServerStartingEvent;
import com.xiaoniucode.etp.server.manager.AccessTokenManager;
import com.xiaoniucode.etp.server.manager.ClientManager;
import com.xiaoniucode.etp.server.manager.ProxyManager;
import com.xiaoniucode.etp.server.web.domain.AccessToken;
import com.xiaoniucode.etp.server.web.domain.Client;
import com.xiaoniucode.etp.server.web.domain.Proxy;
import com.xiaoniucode.etp.server.web.repository.AccessTokenRepository;
import com.xiaoniucode.etp.server.web.repository.ClientRepository;
import com.xiaoniucode.etp.server.web.repository.ProxyRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 将数据库中的配置全部加载到内存
 * 备注：适用于小规模数据场景
 */
@Component
public class DatabaseConfigLoader implements EventListener<TunnelServerStartingEvent> {
    private final Logger logger = LoggerFactory.getLogger(DatabaseConfigLoader.class);
    @Autowired
    private EventBus eventBus;
    @Resource
    private AppConfig appConfig;
    @Autowired
    private AccessTokenRepository accessTokenRepository;
    @Autowired
    private ProxyRepository proxyRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private ProxyManager proxyManager;
    @Autowired
    private AccessTokenManager accessTokenManager;
    @Autowired
    private ClientManager clientManager;

    @PostConstruct
    public void init() {
        eventBus.register(this);
    }

    @Override
    public void onEvent(TunnelServerStartingEvent event) {
        if (!appConfig.getDashboard().getEnable()) {
            logger.warn("管理面板未启用，跳过数据库代理配置加载");
            return;
        }
        logger.debug("准备从数据库加载代理配置信息到缓存");
        loadAllConfigurations();
        logger.debug("数据库配置已成功加载到缓存");
    }

    private void loadAllConfigurations() {
        // 加载访问令牌
        loadAccessTokens();
        // 加载客户端配置
        loadClientConfigs();
        // 加载代理配置
        loadProxyConfigs();
    }

    private void loadProxyConfigs() {
        List<Proxy> proxies = proxyRepository.findAll();
        for (Proxy proxy : proxies) {

        }
    }

    private void loadClientConfigs() {
        List<Client> clients = clientRepository.findAll();
        for (Client client : clients) {

        }
    }

    private void loadAccessTokens() {
        List<AccessToken> accessTokens = accessTokenRepository.findAll();
        for (AccessToken accessToken : accessTokens) {
            if (accessTokenManager.containsToken(accessToken.getToken())) {
                logger.warn("已经存在相同令牌: {}，加载到缓存失败", accessToken.getToken());
                continue;
            }
            AccessTokenInfo accessTokenInfo = new AccessTokenInfo(accessToken.getName(),
                    accessToken.getToken(),
                    accessToken.getMaxClient());
            accessTokenManager.addAccessToken(accessTokenInfo);
            logger.debug("加载访问令牌到缓存：[名称={}，令牌={}，最大连接数={}]", accessToken.getName(), accessToken.getToken(), accessToken.getMaxClient());
        }
    }
}
