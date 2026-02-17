package com.xiaoniucode.etp.server.web.service.impl;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.enums.ClientType;
import com.xiaoniucode.etp.core.enums.DomainType;
import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.core.enums.ProxyStatus;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.generator.GlobalIdGenerator;
import com.xiaoniucode.etp.server.manager.DomainManager;
import com.xiaoniucode.etp.server.manager.PortManager;
import com.xiaoniucode.etp.server.manager.ProxyManager;
import com.xiaoniucode.etp.server.manager.domain.AutoDomainInfo;
import com.xiaoniucode.etp.server.manager.domain.CustomDomainInfo;
import com.xiaoniucode.etp.server.manager.domain.DomainInfo;
import com.xiaoniucode.etp.server.manager.domain.SubDomainInfo;
import com.xiaoniucode.etp.server.proxy.processor.ProxyConfigProcessorExecutor;
import com.xiaoniucode.etp.server.web.common.BizException;
import com.xiaoniucode.etp.server.web.controller.client.response.ClientDTO;
import com.xiaoniucode.etp.server.web.controller.proxy.convert.HttpProxyConvert;
import com.xiaoniucode.etp.server.web.controller.proxy.convert.TcpProxyConvert;
import com.xiaoniucode.etp.server.web.controller.proxy.request.*;
import com.xiaoniucode.etp.server.web.controller.proxy.response.DomainWithBaseDomain;
import com.xiaoniucode.etp.server.web.controller.proxy.response.HttpProxyDTO;
import com.xiaoniucode.etp.server.web.controller.proxy.response.TcpProxyDTO;
import com.xiaoniucode.etp.server.web.entity.Proxy;
import com.xiaoniucode.etp.server.web.entity.ProxyDomain;
import com.xiaoniucode.etp.server.web.repository.ProxyDomainRepository;
import com.xiaoniucode.etp.server.web.repository.ProxyRepository;
import com.xiaoniucode.etp.server.web.service.ClientService;
import com.xiaoniucode.etp.server.web.service.ProxyService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 代理服务实现类
 */
@Service
@RequiredArgsConstructor
public class ProxyServiceImpl implements ProxyService {

    private final ProxyRepository proxyRepository;
    private final ProxyDomainRepository proxyDomainRepository;
    @Resource
    private AppConfig appConfig;
    @Autowired
    private ProxyManager proxyManager;
    @Autowired
    private ClientService clientService;
    @Autowired
    private DomainManager domainManager;
    @Autowired
    private PortManager portManager;
    @Autowired
    private ProxyConfigProcessorExecutor processorExecutor;

    /**
     * 创建 TCP 代理
     */
    @Transactional(rollbackFor = Exception.class)
    public void createTcpProxy(TcpProxyCreateRequest request) {
        String clientId = request.getClientId();
        ClientDTO client = clientService.findById(clientId);
        if (client == null) {
            throw new BizException("客户端不存在");
        }

        Integer remotePort = request.getRemotePort();
        if (remotePort == null || remotePort < 1) {
            remotePort = portManager.acquire();
        } else {
            if (!portManager.isAvailable(remotePort)) {
                throw new BizException("公网端口不可用：" + remotePort);
            }
        }
        if (proxyRepository.findByClientIdAndName(clientId, request.getName()) != null) {
            throw new BizException("名称重复");
        }
        String proxyId = GlobalIdGenerator.uuid32();
        Proxy newProxy = new Proxy();
        newProxy.setId(proxyId);
        newProxy.setClientId(request.getClientId());
        newProxy.setName(request.getName());
        newProxy.setProtocol(ProtocolType.TCP);
        newProxy.setClientType(ClientType.fromCode(client.getClientType()));
        newProxy.setLocalIp(request.getLocalIp());
        newProxy.setLocalPort(request.getLocalPort());
        newProxy.setRemotePort(remotePort);
        newProxy.setStatus(ProxyStatus.fromStatus(request.getStatus()));
        newProxy.setEncrypt(request.getEncrypt());
        newProxy.setCompress(request.getCompress());

        proxyRepository.save(newProxy);
        executeProxyCreation(clientId, buildTcpProxyConfig(newProxy));
    }


    public void executeProxyCreation(String clientId, ProxyConfig config) {
        proxyManager.addProxy(clientId, config, proxyConfig -> processorExecutor.execute(proxyConfig));
    }

    /**
     * 创建 HTTP 代理
     */
    @Transactional(rollbackFor = Exception.class)
    public void createHttpProxy(HttpProxyCreateRequest request) {
        String clientId = request.getClientId();
        ClientDTO client = clientService.findById(clientId);
        if (client == null) {
            throw new BizException("客户端不存在");
        }
        if (proxyRepository.findByClientIdAndName(clientId, request.getName()) != null) {
            throw new BizException("名称已经存在了，请更换一个");
        }
        String proxyId = GlobalIdGenerator.uuid32();
        Proxy newProxy = new Proxy();
        newProxy.setId(proxyId);
        newProxy.setClientId(request.getClientId());
        newProxy.setName(request.getName());
        newProxy.setProtocol(ProtocolType.HTTP);
        newProxy.setClientType(ClientType.fromCode(client.getClientType()));
        newProxy.setLocalIp(request.getLocalIp());
        newProxy.setLocalPort(request.getLocalPort());
        newProxy.setStatus(ProxyStatus.fromStatus(request.getStatus()));
        newProxy.setDomainType(DomainType.fromType(request.getDomainType()));
        newProxy.setEncrypt(request.getEncrypt());
        newProxy.setCompress(request.getCompress());

        proxyRepository.save(newProxy);

        ProxyConfig proxyConfig = buildHttpProxyConfig(proxyId, request);
        proxyManager.addProxy(clientId, proxyConfig, config -> {
            Set<DomainInfo> domains = domainManager.getDomains(config.getProxyId());
            List<ProxyDomain> batch = domains.stream().map(domainInfo -> {
                ProxyDomain proxyDomain = new ProxyDomain();
                proxyDomain.setProxyId(proxyId);
                if (domainInfo instanceof CustomDomainInfo customDomain) {
                    proxyDomain.setDomain(customDomain.getFullDomain());
                }
                if (domainInfo instanceof SubDomainInfo subDomain) {
                    proxyDomain.setBaseDomain(subDomain.getBaseDomain());
                    proxyDomain.setDomain(subDomain.getSubDomain());
                }
                if (domainInfo instanceof AutoDomainInfo autoDomain) {
                    proxyDomain.setBaseDomain(autoDomain.getBaseDomain());
                    proxyDomain.setDomain(autoDomain.getPrefix());
                }
                return proxyDomain;
            }).toList();
            proxyDomainRepository.saveAllAndFlush(batch);
        });

    }

    private ProxyConfig buildTcpProxyConfig(Proxy proxy) {
        ProxyConfig proxyConfig = new ProxyConfig();
        proxyConfig.setName(proxy.getName());
        proxyConfig.setProxyId(proxy.getId());
        proxyConfig.setLocalIp(proxy.getLocalIp());
        proxyConfig.setLocalPort(proxy.getLocalPort());
        proxyConfig.setStatus(proxy.getStatus());
        proxyConfig.setProtocol(ProtocolType.TCP);
        proxyConfig.setRemotePort(proxy.getRemotePort());
        proxyConfig.setCompress(proxy.getCompress());
        proxyConfig.setEncrypt(proxy.getEncrypt());
        return proxyConfig;
    }

    private ProxyConfig buildHttpProxyConfig(String proxyId, HttpProxyCreateRequest request) {
        ProxyConfig proxyConfig = new ProxyConfig();
        proxyConfig.setName(request.getName());
        proxyConfig.setProxyId(proxyId);
        proxyConfig.setLocalIp(request.getLocalIp());
        proxyConfig.setLocalPort(request.getLocalPort());
        proxyConfig.setStatus(ProxyStatus.fromStatus(request.getStatus()));
        proxyConfig.setProtocol(ProtocolType.HTTP);
        proxyConfig.setRemotePort(null);
        proxyConfig.setCompress(request.getCompress());
        proxyConfig.setEncrypt(request.getEncrypt());

        DomainType domainType = DomainType.fromType(request.getDomainType());
        if (domainType == DomainType.CUSTOM_DOMAIN) {
            proxyConfig.getCustomDomains().addAll(request.getDomains());
        } else if (domainType == DomainType.SUBDOMAIN) {
            proxyConfig.getSubDomains().addAll(request.getDomains());
        } else if (domainType == DomainType.AUTO) {
            proxyConfig.setAutoDomain(true);
        }
        return proxyConfig;
    }

    @Override
    @Transactional
    public void updateTcpProxy(TcpProxyUpdateRequest request) {
        //获取现有代理
        Proxy existingProxy = proxyRepository.findById(request.getId()).orElseThrow(() -> new RuntimeException("代理不存在"));

        //更新代理信息
        existingProxy.setName(request.getName());
        existingProxy.setLocalIp(request.getLocalIp());
        existingProxy.setLocalPort(request.getLocalPort());
        existingProxy.setRemotePort(request.getRemotePort());
        existingProxy.setStatus(request.getStatus() == 1 ? ProxyStatus.OPEN : ProxyStatus.CLOSED);
        existingProxy.setEncrypt(request.getEncrypt());
        existingProxy.setCompress(request.getCompress());

        //保存更新
        proxyRepository.save(existingProxy);

    }

    @Override
    @Transactional
    public void updateHttpProxy(HttpProxyUpdateRequest request) {
        //获取现有代理
        Proxy existingProxy = proxyRepository.findById(request.getId()).orElseThrow(() -> new RuntimeException("代理不存在"));

        //更新代理信息
        existingProxy.setName(request.getName());
        existingProxy.setLocalIp(request.getLocalIp());
        existingProxy.setLocalPort(request.getLocalPort());
        existingProxy.setStatus(request.getStatus() == 1 ? ProxyStatus.OPEN : ProxyStatus.CLOSED);
        existingProxy.setDomainType(DomainType.values()[request.getDomainType()]);
        existingProxy.setEncrypt(request.getEncrypt());
        existingProxy.setCompress(request.getCompress());

        //保存更新
        proxyRepository.save(existingProxy);

        //删除现有域名
        proxyDomainRepository.deleteAll(proxyDomainRepository.findByProxyId(request.getId()));

        //批量持久化新域名
        if (request.getDomains() != null && !request.getDomains().isEmpty()) {
            List<ProxyDomain> proxyDomains = request.getDomains().stream()
                    .map(domain -> {
                        ProxyDomain proxyDomain = new ProxyDomain();
                        proxyDomain.setProxyId(request.getId());
                        proxyDomain.setDomain(domain);
                        return proxyDomain;
                    })
                    .collect(Collectors.toList());
            proxyDomainRepository.saveAll(proxyDomains);
        }

    }


    @Override
    public TcpProxyDTO getTcpProxyById(String id) {
        Proxy proxy = proxyRepository.findById(id).orElse(null);
        return proxy != null ? TcpProxyConvert.INSTANCE.toDTO(proxy) : null;
    }

    @Override
    public HttpProxyDTO getHttpProxyById(String id) {
        Proxy proxy = proxyRepository.findById(id).orElse(null);
        if (proxy == null) {
            return null;
        }
        List<ProxyDomain> proxyDomains = proxyDomainRepository.findByProxyId(id);
        List<DomainWithBaseDomain> domains = proxyDomains.stream()
                .map(proxyDomain -> new DomainWithBaseDomain(proxyDomain.getDomain(), proxyDomain.getBaseDomain()))
                .collect(Collectors.toList());
        int httpProxyPort = appConfig.getHttpProxyPort();
        return HttpProxyConvert.INSTANCE.toDTO(proxy, domains, httpProxyPort);
    }

    @Override
    public List<TcpProxyDTO> getTcpProxies() {
        List<Proxy> proxies = proxyRepository.findByProtocol(ProtocolType.TCP);
        return TcpProxyConvert.INSTANCE.toDTOList(proxies);
    }

    @Override
    public List<HttpProxyDTO> getHttpProxies() {
        List<Proxy> proxies = proxyRepository.findByProtocol(ProtocolType.HTTP);
        return proxies.stream()
                .map(proxy -> {
                    List<ProxyDomain> proxyDomains = proxyDomainRepository.findByProxyId(proxy.getId());
                    List<DomainWithBaseDomain> domains = proxyDomains.stream()
                            .map(proxyDomain -> new DomainWithBaseDomain(proxyDomain.getDomain(), proxyDomain.getBaseDomain()))
                            .collect(Collectors.toList());
                    int httpProxyPort = appConfig.getHttpProxyPort();
                    return HttpProxyConvert.INSTANCE.toDTO(proxy, domains, httpProxyPort);
                })
                .collect(Collectors.toList());
    }

    /**
     * 删除代理
     */
    @Transactional
    public void deleteProxy(String proxyId) {
        if (!proxyRepository.existsById(proxyId)) {
            throw new BizException("代理不存在: " + proxyId);
        }
        proxyDomainRepository.deleteAll(proxyDomainRepository.findByProxyId(proxyId));
        proxyRepository.deleteById(proxyId);
        proxyManager.removeProxyById(proxyId).ifPresent(c -> {
            c.setStatus(ProxyStatus.DELETED);
            processorExecutor.execute(c);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteProxies(BatchDeleteRequest request) {
        Set<String> ids = request.getIds();
        //删除基础信息
        proxyRepository.deleteAllById(ids);
        //删除域名信息
        proxyDomainRepository.deleteByProxyIdIn(ids);
        //删除内存状态
        proxyManager.removeProxies(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void switchProxyStatus(String id) {
        proxyRepository.findById(id).ifPresent(proxy -> {
            ProxyStatus status = proxy.getStatus();
            proxy.setStatus(status.toggle());
            //更新内存状态
            ProxyConfig proxyConfig = proxyManager.changeStatus(proxy.getId(), proxy.getStatus());
            processorExecutor.execute(proxyConfig);
            proxyRepository.saveAndFlush(proxy);
        });
    }
}