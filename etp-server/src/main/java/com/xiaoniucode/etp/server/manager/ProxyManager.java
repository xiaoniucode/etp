package com.xiaoniucode.etp.server.manager;

import com.xiaoniucode.etp.common.utils.StringUtils;
import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.server.config.domain.ClientInfo;
import com.xiaoniucode.etp.server.manager.temp.DomainGenerator;
import com.xiaoniucode.etp.server.manager.temp.DomainManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Component
public class ProxyManager {
    private static final Logger logger = LoggerFactory.getLogger(ProxyManager.class);
    /**
     * remotePort --> ProxyConfig
     * 代理端口与代理配置映射
     */
    private final Map<Integer, ProxyConfig> portToProxyConfig = new ConcurrentHashMap<>();
    /**
     * domain --> ProxyConfig
     * 域名与代理配置映射
     */
    private final Map<String, ProxyConfig> domainToProxyConfig = new ConcurrentHashMap<>();
    /**
     * clientId --> ProxyConfigs
     * 每个客户端的配置列表
     */
    private final Map<String, Set<ProxyConfig>> clientIdToProxyConfigs = new ConcurrentHashMap<>();
    @Autowired
    private ClientManager clientManager;
    @Autowired
    private DomainManager domainManager;
    @Autowired
    private DomainGenerator domainGenerator;

    public synchronized ProxyConfig addProxy(String clientId, ProxyConfig proxyConfig) {
        return addProxy(clientId, proxyConfig, null);
    }

    public synchronized ProxyConfig addProxy(String clientId, ProxyConfig proxyConfig, Consumer<ProxyConfig> callback) {
        if (!StringUtils.hasText(clientId) || proxyConfig == null) {
            return null;
        }
        ProtocolType protocol = proxyConfig.getProtocol();
        if (ProtocolType.isTcp(protocol)) {
            Integer remotePort = proxyConfig.getRemotePort();
            if (portToProxyConfig.containsKey(remotePort)) {
                logger.warn("端口冲突 [端口={}] 代理创建失败 [客户端ID={}，代理名称={}]",
                        remotePort, clientId, proxyConfig.getName());
                ProxyConfig config = portToProxyConfig.get(remotePort);
                callback.accept(config);
                return config;
            }
            portToProxyConfig.put(proxyConfig.getRemotePort(), proxyConfig);
            logger.debug("代理创建成功: [客户端ID={}，代理名称={}，远程端口={}，内网端口={}]", clientId, proxyConfig.getName(), remotePort, proxyConfig.getLocalPort());
        }
        if (ProtocolType.isHttp(protocol)) {
            Set<String> domains = proxyConfig.getFullDomains();
            for (String domain : domains) {
                if (domainToProxyConfig.containsKey(domain)) {
                    logger.warn("域名冲突[域名={}] 该域名添加失败 [客户端ID={}，代理名称={}]",
                            domain, clientId, proxyConfig.getName());
                    continue;
                }
                domainToProxyConfig.put(domain, proxyConfig);
            }
            logger.debug("代理创建成功: [客户端ID={},代理名称={},域名={}]", clientId, proxyConfig.getName(), domains);
        }
        ClientInfo clientInfo = clientManager.getClient(clientId);
        if (clientInfo != null) {
            //clientInfo: proxyName -> ProxyConfig
            clientInfo.getProxyNameToProxyConfig().put(proxyConfig.getName(), proxyConfig);
        } else {
            //todo
        }
        clientIdToProxyConfigs.computeIfAbsent(clientId, k ->
                ConcurrentHashMap.newKeySet()).add(proxyConfig);
        if (callback != null) {
            callback.accept(proxyConfig);
        }
        return proxyConfig;
    }

    public ProxyConfig removeProxy(String clientId, String proxyName) {
        return removeProxy(clientId, proxyName, null);
    }

    public synchronized ProxyConfig removeProxy(String clientId, String proxyName, Consumer<ProxyConfig> callback) {
        ClientInfo clientInfo = clientManager.getClient(clientId);
        if (clientInfo == null) {
            return null;
        }
        ProxyConfig proxyConfig = clientInfo.getProxyNameToProxyConfig().get(proxyName);
        if (proxyConfig == null) {
            logger.warn("删除代理失败，代理配置不存在");
            return null;
        }
        ProtocolType protocol = proxyConfig.getProtocol();
        if (ProtocolType.isTcp(protocol)) {
            portToProxyConfig.remove(proxyConfig.getRemotePort());
        }
        if (ProtocolType.isHttp(protocol)) {
            Set<String> domains = proxyConfig.getFullDomains();
            if (domains != null && !domains.isEmpty()) {
                for (String domain : domains) {
                    domainToProxyConfig.remove(domain);
                }
                domains.clear();
            }
        }
        Set<ProxyConfig> proxyConfigs = clientIdToProxyConfigs.get(clientId);
        if (proxyConfigs != null && !proxyConfigs.isEmpty()) {
            Iterator<ProxyConfig> iterator = proxyConfigs.iterator();
            while (iterator.hasNext()) {
                ProxyConfig config = iterator.next();
                if (proxyName.equals(config.getName())) {
                    iterator.remove();
                    break;
                }
            }
            if (proxyConfigs.isEmpty()) {
                clientIdToProxyConfigs.remove(clientId);
            }
        }
        logger.debug("代理删除成功: [客户端ID={},代理名称={}]", clientId, proxyConfig.getName());
        if (callback != null) {
            callback.accept(proxyConfig);
        }
        return proxyConfig;
    }

    public boolean hasDomain(String domain) {
        return domainToProxyConfig.containsKey(domain);
    }

    public ProxyConfig getByRemotePort(Integer port) {
        return portToProxyConfig.get(port);
    }

    public Collection<ProxyConfig> getTcpProxyConfigs() {
        return portToProxyConfig.values();
    }

    public ProxyConfig getByDomain(String domain) {
        return domainToProxyConfig.get(domain);
    }

    public Set<ProxyConfig> getByClientId(String clientId) {
        return clientIdToProxyConfigs.getOrDefault(clientId, new HashSet<>());
    }

    /**
     * 判断代理是否已经存在
     *
     * @param clientId 客户端标识
     * @param config   配置信息
     * @return 是否存在
     */
    public Boolean hasProxy(String clientId, ProxyConfig config) {
        ClientInfo client = clientManager.getClient(clientId);
        if (client == null) {
            logger.error("客户端不存在: {}", clientId);
            return null;
        }
        return client.hasName(config.getName());
    }
}
