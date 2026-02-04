package com.xiaoniucode.etp.server.web.listener;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.core.notify.EventListener;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.config.domain.AccessTokenInfo;
import com.xiaoniucode.etp.server.config.domain.ClientInfo;
import com.xiaoniucode.etp.server.event.TunnelServerStartingEvent;
import com.xiaoniucode.etp.server.manager.*;
import com.xiaoniucode.etp.server.web.domain.AccessToken;
import com.xiaoniucode.etp.server.web.domain.Client;
import com.xiaoniucode.etp.server.web.domain.Proxy;
import com.xiaoniucode.etp.server.web.domain.ProxyDomain;
import com.xiaoniucode.etp.server.web.repository.AccessTokenRepository;
import com.xiaoniucode.etp.server.web.repository.ClientRepository;
import com.xiaoniucode.etp.server.web.repository.ProxyDomainRepository;
import com.xiaoniucode.etp.server.web.repository.ProxyRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * 如果开启管理面板，则将数据库中的配置全部加载到内存
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
    private ProxyDomainRepository proxyDomainRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private ProxyManager proxyManager;
    @Autowired
    private AccessTokenManager accessTokenManager;
    @Autowired
    private ClientManager clientManager;
    @Autowired
    private DomainManager domainManager;
    @Autowired
    private PortManager portManager;

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
       // loadProxyConfigs();
    }

    private void loadProxyConfigs() {
        List<Proxy> proxies = proxyRepository.findAll();
        for (Proxy proxy : proxies) {
            String clientId = proxy.getClientId();
            List<ProxyDomain> proxyDomain = proxyDomainRepository.findByProxyId(proxy.getId());
            List<String> domains = proxyDomain.stream().map(ProxyDomain::getDomain).toList();

            Set<String> d = domainManager.addDomains(domains);
            ProxyConfig proxyConfig = toProxyConfig(proxy);

            proxyConfig.getFullDomains().addAll(d);
            proxyManager.addProxy(clientId, proxyConfig);
        }
    }

    private ProxyConfig toProxyConfig(Proxy proxy) {
        ProxyConfig config = new ProxyConfig();
        config.setName(proxy.getName());
        config.setLocalIp(proxy.getLocalIp());
        config.setLocalPort(proxy.getLocalPort());
        config.setRemotePort(proxy.getRemotePort());
        config.setProtocol(proxy.getProtocol());
        config.setStatus(proxy.getStatus());
        config.setEncrypt(proxy.getEncrypt());
        config.setCompress(proxy.getCompress());
        return config;
    }

    private void loadClientConfigs() {
        logger.debug("开始加载客户端配置...");
        List<Client> clients = clientRepository.findAll();
        int total = clients.size();
        int successCount = 0;
        int skipCount = 0;
        logger.debug("从数据库查询到 {} 个客户端配置", clients.size());
        for (Client client : clients) {
            if (clientManager.hasClient(client.getClientId())) {
                skipCount++;
                logger.warn("客户端缓存失败，已存在相同客户端 - 客户端ID：{}", client.getClientId());
                continue;
            }
            ClientInfo clientInfo = new ClientInfo();
            clientInfo.setClientId(clientInfo.getClientId());
            clientInfo.setName(client.getName());
            clientInfo.setOs(client.getOs());
            clientInfo.setArch(client.getArch());
            clientInfo.setVersion(clientInfo.getVersion());
            clientManager.addClient(clientInfo);
            successCount++;
            logger.debug("加载客户端配置到缓存：[客户端ID={}，客户端名字={}]", client.getClientId(), client.getName());
        }
        logger.debug("客户端配置加载完成。成功: {}, 跳过: {}, 总计: {}",
                successCount, skipCount, total);
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
