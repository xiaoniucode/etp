package com.xiaoniucode.etp.server.web.service.impl;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.enums.ClientType;
import com.xiaoniucode.etp.core.enums.DomainType;
import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.core.enums.ProxyStatus;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.generator.GlobalIdGenerator;
import com.xiaoniucode.etp.server.manager.DomainGenerator;
import com.xiaoniucode.etp.server.manager.DomainManager;
import com.xiaoniucode.etp.server.manager.ProxyManager;
import com.xiaoniucode.etp.server.manager.domain.AutoDomainInfo;
import com.xiaoniucode.etp.server.manager.domain.CustomDomainInfo;
import com.xiaoniucode.etp.server.manager.domain.DomainInfo;
import com.xiaoniucode.etp.server.manager.domain.SubDomainInfo;
import com.xiaoniucode.etp.server.web.common.BizException;
import com.xiaoniucode.etp.server.web.controller.client.response.ClientDTO;
import com.xiaoniucode.etp.server.web.controller.proxy.convert.HttpProxyConvert;
import com.xiaoniucode.etp.server.web.controller.proxy.convert.TcpProxyConvert;
import com.xiaoniucode.etp.server.web.controller.proxy.request.*;
import com.xiaoniucode.etp.server.web.controller.proxy.response.DomainWithBaseDomain;
import com.xiaoniucode.etp.server.web.controller.proxy.response.HttpProxyDTO;
import com.xiaoniucode.etp.server.web.controller.proxy.response.TcpProxyDTO;
import com.xiaoniucode.etp.server.web.domain.Proxy;
import com.xiaoniucode.etp.server.web.domain.ProxyDomain;
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
import java.util.function.Consumer;
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

    /**
     * 创建 TCP 代理
     */
    @Transactional
    public void createTcpProxy(TcpProxyCreateRequest proxy) {
        String id = GlobalIdGenerator.uuid32();
        String clientId = proxy.getClientId();
        ClientDTO client = clientService.findById(clientId);
        //创建代理对象
        Proxy newProxy = new Proxy();
        newProxy.setId(id);
        newProxy.setClientId(proxy.getClientId());
        newProxy.setName(proxy.getName());
        newProxy.setProtocol(ProtocolType.TCP);
        newProxy.setClientType(ClientType.fromCode(client.getClientType()));
        newProxy.setLocalIp(proxy.getLocalIp());
        newProxy.setLocalPort(proxy.getLocalPort());
        newProxy.setRemotePort(proxy.getRemotePort());
        newProxy.setStatus(ProxyStatus.fromStatus(proxy.getStatus()));
        newProxy.setEncrypt(proxy.getEncrypt());
        newProxy.setCompress(proxy.getCompress());
        //保存代理信息
        proxyRepository.save(newProxy);

    }

    /**
     * 创建 HTTP 代理
     */
    @Transactional(rollbackFor = Exception.class)
    public void createHttpProxy(HttpProxyCreateRequest proxy) {
        String proxyId = GlobalIdGenerator.uuid32();
        if (proxyRepository.findByName(proxy.getName()) != null) {
            throw new BizException("名称已经存在了，请更换一个");
        }
        String clientId = proxy.getClientId();
        ClientDTO client = clientService.findById(clientId);
        if (client == null) {
            throw new BizException("客户端不存在");
        }
        Proxy newProxy = new Proxy();
        newProxy.setId(proxyId);
        newProxy.setClientId(proxy.getClientId());
        newProxy.setName(proxy.getName());
        newProxy.setProtocol(ProtocolType.HTTP);
        newProxy.setClientType(ClientType.fromCode(client.getClientType()));
        newProxy.setLocalIp(proxy.getLocalIp());
        newProxy.setLocalPort(proxy.getLocalPort());
        newProxy.setStatus(ProxyStatus.fromStatus(proxy.getStatus()));
        newProxy.setDomainType(DomainType.fromType(proxy.getDomainType()));
        newProxy.setEncrypt(proxy.getEncrypt());
        newProxy.setCompress(proxy.getCompress());

        proxyRepository.save(newProxy);

        ProxyConfig proxyConfig = buildHttpProxyConfig(proxyId, proxy);
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

    private ProxyConfig buildHttpProxyConfig(String proxyId, HttpProxyCreateRequest proxy) {
        DomainType domainType = DomainType.fromType(proxy.getDomainType());
        ProxyConfig proxyConfig = new ProxyConfig();
        proxyConfig.setName(proxy.getName());
        proxyConfig.setProxyId(proxyId);
        proxyConfig.setLocalIp(proxy.getLocalIp());
        proxyConfig.setLocalPort(proxy.getLocalPort());
        proxyConfig.setStatus(ProxyStatus.fromStatus(proxy.getStatus()));
        proxyConfig.setProtocol(ProtocolType.HTTP);
        if (domainType == DomainType.CUSTOM_DOMAIN) {
            proxyConfig.getCustomDomains().addAll(proxy.getDomains());
        } else if (domainType == DomainType.SUBDOMAIN) {
            proxyConfig.getSubDomains().addAll(proxy.getDomains());
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

    /**
     * 删除代理
     */
    @Transactional
    public void deleteProxy(String id) {
        // 删除关联的域名
        proxyDomainRepository.deleteAll(proxyDomainRepository.findByProxyId(id));
        // 删除代理
        proxyRepository.deleteById(id);
        //释放域名资源占用
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteProxies(BatchDeleteRequest request) {
        Set<String> ids = request.getIds();
        //删除基础信息
        proxyRepository.deleteAllById(ids);
        //删除域名信息
        proxyDomainRepository.deleteByProxyIdIn(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void switchProxyStatus(String id) {
        proxyRepository.findById(id).ifPresent(proxy -> {
            ProxyStatus status = proxy.getStatus();
            if (ProxyStatus.OPEN == status) {
                proxy.setStatus(ProxyStatus.CLOSED);
            } else {
                proxy.setStatus(ProxyStatus.OPEN);
            }
            //todo 更新内存状态
            proxyRepository.saveAndFlush(proxy);
        });
    }

}