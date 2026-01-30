package com.xiaoniucode.etp.server.web.service;

import com.xiaoniucode.etp.server.web.domain.Proxy;

import java.util.List;

public interface ProxyService {
    Proxy getProxy(Proxy req);

    List<Proxy> proxies(String type);

    Proxy addTcpProxy(Proxy req);

    void updateTcpProxy(Proxy req);

    Proxy addHttpsProxy(Proxy req);

    Proxy addHttpProxy(Proxy req);

    void switchProxyStatus(Proxy req);

    void deleteProxy(Proxy req);

    void deleteProxiesByClient(int clientId);
}
