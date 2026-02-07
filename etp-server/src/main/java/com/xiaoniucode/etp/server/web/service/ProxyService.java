package com.xiaoniucode.etp.server.web.service;

import com.xiaoniucode.etp.server.web.controller.proxy.request.HttpProxyCreateRequest;
import com.xiaoniucode.etp.server.web.controller.proxy.request.HttpProxyUpdateRequest;
import com.xiaoniucode.etp.server.web.controller.proxy.request.TcpProxyCreateRequest;
import com.xiaoniucode.etp.server.web.controller.proxy.request.TcpProxyUpdateRequest;
import com.xiaoniucode.etp.server.web.controller.proxy.response.HttpProxyDTO;
import com.xiaoniucode.etp.server.web.controller.proxy.response.TcpProxyDTO;

import java.util.List;

public interface ProxyService {

    /**
     * 创建 TCP 代理
     */
    void createTcpProxy(TcpProxyCreateRequest proxy);

    /**
     * 创建 HTTP 代理
     */
    void createHttpProxy(HttpProxyCreateRequest proxy);

    /**
     * 更新 TCP 代理
     */
    void updateTcpProxy(TcpProxyUpdateRequest request);

    /**
     * 更新 HTTP 代理
     */
    void updateHttpProxy(HttpProxyUpdateRequest request);

    /**
     * 删除代理
     */
    void deleteProxy(Integer id);

    /**
     * 根据 ID 查询tcp代理
     */
    TcpProxyDTO getTcpProxyById(Integer id);

    HttpProxyDTO getHttpProxyById(Integer id);

    /**
     * 根据 ID 查询http代理
     */
    List<TcpProxyDTO> getTcpProxies();

    List<HttpProxyDTO> getHttpProxies();

    void batchDeleteProxies(List<Integer> ids);

    void switchProxyStatus(Integer id);

}