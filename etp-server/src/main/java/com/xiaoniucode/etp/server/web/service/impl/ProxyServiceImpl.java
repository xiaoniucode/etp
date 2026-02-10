package com.xiaoniucode.etp.server.web.service.impl;

import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.core.enums.ProxyStatus;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.generator.GlobalIdGenerator;
import com.xiaoniucode.etp.server.manager.ProxyManager;
import com.xiaoniucode.etp.server.web.controller.proxy.convert.HttpProxyConvert;
import com.xiaoniucode.etp.server.web.controller.proxy.convert.TcpProxyConvert;
import com.xiaoniucode.etp.server.web.controller.proxy.request.HttpProxyCreateRequest;
import com.xiaoniucode.etp.server.web.controller.proxy.request.HttpProxyUpdateRequest;
import com.xiaoniucode.etp.server.web.controller.proxy.request.TcpProxyCreateRequest;
import com.xiaoniucode.etp.server.web.controller.proxy.request.TcpProxyUpdateRequest;
import com.xiaoniucode.etp.server.web.controller.proxy.response.HttpProxyDTO;
import com.xiaoniucode.etp.server.web.controller.proxy.response.TcpProxyDTO;
import com.xiaoniucode.etp.server.web.domain.Proxy;
import com.xiaoniucode.etp.server.web.domain.ProxyDomain;
import com.xiaoniucode.etp.server.web.repository.ProxyDomainRepository;
import com.xiaoniucode.etp.server.web.repository.ProxyRepository;
import com.xiaoniucode.etp.server.web.service.ProxyService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    /**
     * 创建 TCP 代理
     */
    @Transactional
    public void createTcpProxy(TcpProxyCreateRequest proxy) {
        //判断客户端是否存在
        String id = GlobalIdGenerator.uuid32();

    }

    /**
     * 创建 HTTP 代理
     */
    @Transactional
    public void createHttpProxy(HttpProxyCreateRequest proxy) {
        //保存基本信息
        //批量持久化域名
        String id = GlobalIdGenerator.uuid32();
    }

    @Override
    @Transactional
    public void updateTcpProxy(TcpProxyUpdateRequest request) {
        //释放域名资源占用

    }

    @Override
    @Transactional
    public void updateHttpProxy(HttpProxyUpdateRequest request) {
        //释放域名资源占用
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
        // 查询关联的域名列表
        List<ProxyDomain> proxyDomains = proxyDomainRepository.findByProxyId(id);
        List<String> domains = proxyDomains.stream()
                .map(ProxyDomain::getDomain)
                .collect(java.util.stream.Collectors.toList());
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
                    List<String> domains = proxyDomains.stream()
                            .map(ProxyDomain::getDomain)
                            .collect(java.util.stream.Collectors.toList());
                    int httpProxyPort = appConfig.getHttpProxyPort();
                    return HttpProxyConvert.INSTANCE.toDTO(proxy, domains, httpProxyPort);
                })
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public void batchDeleteProxies(List<String> ids) {

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void switchProxyStatus(String id) {
        proxyRepository.findById(id).ifPresent(proxy -> {
            ProxyStatus status = proxy.getStatus();
            if (ProxyStatus.OPEN==status){
                proxy.setStatus(ProxyStatus.CLOSED);
            }else {
                proxy.setStatus(ProxyStatus.OPEN);
            }
            //todo 更新内存状态

            proxyRepository.saveAndFlush(proxy);
        });
    }

}