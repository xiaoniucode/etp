package com.xiaoniucode.etp.server.web.service.impl;

import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.server.web.controller.proxy.request.HttpProxyCreateRequest;
import com.xiaoniucode.etp.server.web.controller.proxy.request.HttpProxyUpdateRequest;
import com.xiaoniucode.etp.server.web.controller.proxy.request.TcpProxyCreateRequest;
import com.xiaoniucode.etp.server.web.controller.proxy.request.TcpProxyUpdateRequest;
import com.xiaoniucode.etp.server.web.domain.Proxy;
import com.xiaoniucode.etp.server.web.domain.ProxyDomain;
import com.xiaoniucode.etp.server.web.repository.ProxyDomainRepository;
import com.xiaoniucode.etp.server.web.repository.ProxyRepository;
import com.xiaoniucode.etp.server.web.service.ProxyService;
import lombok.RequiredArgsConstructor;
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

    /**
     * 创建 TCP 代理
     */
    @Transactional
    public Proxy createTcpProxy(TcpProxyCreateRequest proxy) {
        return null;
    }

    /**
     * 创建 HTTP 代理
     */
    @Transactional
    public Proxy createHttpProxy(HttpProxyCreateRequest proxy) {
        //保存基本信息
        //批量持久化域名
        return null;
    }

    @Override
    @Transactional
    public Proxy updateTcpProxy(TcpProxyUpdateRequest request) {
        return null;
    }

    @Override
    @Transactional
    public Proxy updateHttpProxy(HttpProxyUpdateRequest request) {
        return null;
    }

    /**
     * 删除代理
     */
    @Transactional
    public void deleteProxy(Integer id) {
        // 删除关联的域名
        proxyDomainRepository.deleteAll(proxyDomainRepository.findByProxyId(id));
        // 删除代理
        proxyRepository.deleteById(id);
    }

    /**
     * 根据 ID 查询代理
     */
    public Proxy getProxyById(Integer id) {
        return proxyRepository.findById(id).orElse(null);
    }

    /**
     * 根据客户端 ID 查询代理
     */
    public List<Proxy> getProxiesByClientId(String clientId) {
        return proxyRepository.findByClientId(Integer.valueOf(clientId));
    }

    /**
     * 获取所有 TCP 代理
     */
    public List<Proxy> getTcpProxies() {
        return proxyRepository.findByProtocol(ProtocolType.TCP);
    }

    /**
     * 获取所有 HTTP 代理
     */
    public List<Proxy> getHttpProxies() {
        return proxyRepository.findByProtocol(ProtocolType.HTTP);
    }

    /**
     * 根据代理 ID 获取域名列表
     */
    public List<String> getDomainsByProxyId(Integer proxyId) {
        return proxyDomainRepository.findByProxyId(proxyId).stream()
                .map(ProxyDomain::getDomain)
                .collect(java.util.stream.Collectors.toList());
    }

}
