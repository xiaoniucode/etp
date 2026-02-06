package com.xiaoniucode.etp.server.web.service;

import com.xiaoniucode.etp.server.web.controller.proxy.request.HttpProxyCreateRequest;
import com.xiaoniucode.etp.server.web.controller.proxy.request.HttpProxyUpdateRequest;
import com.xiaoniucode.etp.server.web.controller.proxy.request.TcpProxyCreateRequest;
import com.xiaoniucode.etp.server.web.controller.proxy.request.TcpProxyUpdateRequest;
import com.xiaoniucode.etp.server.web.domain.Proxy;
import com.xiaoniucode.etp.server.web.domain.ProxyDomain;

import java.util.List;

public interface ProxyService {

    /**
     * 创建 TCP 代理
     */
    Proxy createTcpProxy(TcpProxyCreateRequest proxy);

    /**
     * 创建 HTTP 代理
     */
    Proxy createHttpProxy(HttpProxyCreateRequest proxy);

    /**
     * 更新 TCP 代理
     */
    Proxy updateTcpProxy(TcpProxyUpdateRequest request);

    /**
     * 更新 HTTP 代理
     */
    Proxy updateHttpProxy(HttpProxyUpdateRequest request);

    /**
     * 删除代理
     */
    void deleteProxy(Integer id);

    /**
     * 根据 ID 查询代理
     */
    Proxy getProxyById(Integer id);

    /**
     * 根据客户端 ID 查询代理
     */
    List<Proxy> getProxiesByClientId(String clientId);

    /**
     * 获取所有 TCP 代理
     */
    List<Proxy> getTcpProxies();

    /**
     * 获取所有 HTTP 代理
     */
    List<Proxy> getHttpProxies();

    /**
     * 根据代理 ID 获取域名列表
     */
    List<String> getDomainsByProxyId(Integer proxyId);
}
