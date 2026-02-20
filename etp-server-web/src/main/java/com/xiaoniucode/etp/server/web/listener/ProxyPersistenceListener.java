package com.xiaoniucode.etp.server.web.listener;

import com.xiaoniucode.etp.core.domain.AccessControlConfig;
import com.xiaoniucode.etp.core.domain.BasicAuthConfig;
import com.xiaoniucode.etp.core.domain.HttpUser;
import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.enums.AccessControlMode;
import com.xiaoniucode.etp.core.enums.ClientType;
import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.core.notify.EventListener;
import com.xiaoniucode.etp.server.event.ProxyCreatedEvent;
import com.xiaoniucode.etp.server.manager.DomainManager;
import com.xiaoniucode.etp.server.manager.domain.AutoDomainInfo;
import com.xiaoniucode.etp.server.manager.domain.CustomDomainInfo;
import com.xiaoniucode.etp.server.manager.domain.DomainInfo;
import com.xiaoniucode.etp.server.manager.domain.SubDomainInfo;
import com.xiaoniucode.etp.server.web.controller.accesscontrol.dto.AccessControlDTO;
import com.xiaoniucode.etp.server.web.controller.accesscontrol.request.AddAccessControlRequest;
import com.xiaoniucode.etp.server.web.controller.accesscontrol.request.AddAccessControlRuleRequest;
import com.xiaoniucode.etp.server.web.controller.basicauth.request.AddBasicAuthRequest;
import com.xiaoniucode.etp.server.web.controller.basicauth.request.AddHttpUserRequest;
import com.xiaoniucode.etp.server.web.entity.BasicAuth;
import com.xiaoniucode.etp.server.web.entity.Proxy;
import com.xiaoniucode.etp.server.web.entity.ProxyDomain;
import com.xiaoniucode.etp.server.web.repository.ProxyDomainRepository;
import com.xiaoniucode.etp.server.web.repository.ProxyRepository;
import com.xiaoniucode.etp.server.web.service.AccessControlService;
import com.xiaoniucode.etp.server.web.service.BasicAuthService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 */
@Component
public class ProxyPersistenceListener implements EventListener<ProxyCreatedEvent> {
    private final Logger logger = LoggerFactory.getLogger(ProxyPersistenceListener.class);
    @Autowired
    private EventBus eventBus;
    @Autowired
    private DomainManager domainManager;
    @Autowired
    private ProxyRepository proxyRepository;
    @Autowired
    private ProxyDomainRepository proxyDomainRepository;
    @Autowired
    private AccessControlService accessControlService;
    @Autowired
    private BasicAuthService basicAuthService;

    @PostConstruct
    public void init() {
        eventBus.register(this);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onEvent(ProxyCreatedEvent event) {
        String clientId = event.getClientId();
        ClientType clientType = event.getClientType();
        ProxyConfig config = event.getProxyConfig();

        Proxy proxy = toProxy(clientId, clientType, config);
        ProtocolType protocol = config.getProtocol();
        if (ProtocolType.isTcp(protocol)) {
            proxyRepository.saveAndFlush(proxy);
        } else if (ProtocolType.isHttp(protocol)) {
            //保存代理基础配置信息
            Proxy p = proxyRepository.saveAndFlush(proxy);
            List<ProxyDomain> batch = new ArrayList<>();
            Set<String> fullDomains = config.getFullDomains();
            for (String domain : fullDomains) {
                ProxyDomain proxyDomain = new ProxyDomain();
                proxyDomain.setProxyId(p.getId());
                DomainInfo domainInfo = domainManager.getDomainInfo(domain);
                if (domainInfo instanceof CustomDomainInfo customDomain) {
                    String fullDomain = customDomain.getFullDomain();
                    proxyDomain.setDomain(fullDomain);
                } else if (domainInfo instanceof SubDomainInfo subDomain) {
                    proxyDomain.setBaseDomain(subDomain.getBaseDomain());
                    proxyDomain.setDomain(subDomain.getSubDomain());
                } else if (domainInfo instanceof AutoDomainInfo autoDomain) {
                    proxyDomain.setBaseDomain(autoDomain.getBaseDomain());
                    proxyDomain.setDomain(autoDomain.getPrefix());
                }
                batch.add(proxyDomain);
            }
            //批量保存所有域名信息
            proxyDomainRepository.saveAllAndFlush(batch);
        }
        String proxyId = config.getProxyId();
        AccessControlConfig accessControl = config.getAccessControl();
        //初始化访问控制表
        AddAccessControlRequest request = new AddAccessControlRequest();
        request.setProxyId(proxyId);
        request.setMode(AccessControlMode.ALLOW.getCode());
        request.setEnable(false);
        if (accessControl != null) {
            AccessControlMode mode = accessControl.getMode();
            request.setEnable(accessControl.isEnable());
            request.setMode(mode.getCode());
        }
        AccessControlDTO add = accessControlService.add(request);
        if (accessControl != null && add != null) {
            Set<String> allow = accessControl.getAllow();
            Set<String> deny = accessControl.getDeny();
            List<AddAccessControlRuleRequest> rules = new ArrayList<>();
            if (allow != null && !allow.isEmpty()) {
                for (String cidr : allow) {
                    AddAccessControlRuleRequest rule = new AddAccessControlRuleRequest(proxyId, cidr, AccessControlMode.ALLOW.getCode());
                    rules.add(rule);
                }
            }
            if (deny != null && !deny.isEmpty()) {
                for (String cidr : deny) {
                    AddAccessControlRuleRequest rule = new AddAccessControlRuleRequest(proxyId, cidr, AccessControlMode.DENY.getCode());
                    rules.add(rule);
                }
            }
            if (!rules.isEmpty()) {
                accessControlService.addRules(rules);
            }
        }

        //HTTP Basic 认证
        if (ProtocolType.isHttp(config.getProtocol())) {
            AddBasicAuthRequest addBasicAuthRequest = new AddBasicAuthRequest(proxyId, false);
            BasicAuthConfig basicAuth = config.getBasicAuth();
            if (basicAuth != null) {
                addBasicAuthRequest.setEnable(basicAuth.isEnable());
                Set<HttpUser> users = basicAuth.getUsers();
                if (users != null && !users.isEmpty()) {
                    List<AddHttpUserRequest> httpUsers = new ArrayList<>();
                    for (HttpUser user : users) {
                        httpUsers.add(new AddHttpUserRequest(proxyId, user.getUser(), user.getPass()));
                    }
                    basicAuthService.addUsers(httpUsers);
                }
            }
            basicAuthService.addBasicAuth(addBasicAuthRequest);
        }

    }

    private Proxy toProxy(String clientId, ClientType clientType, ProxyConfig config) {
        Proxy proxy = new Proxy();
        proxy.setId(config.getProxyId());
        proxy.setClientId(clientId);
        proxy.setName(config.getName());
        proxy.setProtocol(config.getProtocol());
        proxy.setLocalIp(config.getLocalIp());
        proxy.setLocalPort(config.getLocalPort());
        proxy.setStatus(config.getStatus());
        proxy.setCompress(config.getCompress());
        proxy.setEncrypt(config.getEncrypt());
        proxy.setClientType(clientType);

        if (ProtocolType.isTcp(config.getProtocol())) {
            proxy.setRemotePort(config.getRemotePort());
        }
        if (ProtocolType.isHttp(config.getProtocol())) {
            proxy.setDomainType(config.getDomainType());
        }
        return proxy;
    }
}
