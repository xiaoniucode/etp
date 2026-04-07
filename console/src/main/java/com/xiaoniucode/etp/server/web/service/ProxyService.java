package com.xiaoniucode.etp.server.web.service;

import com.xiaoniucode.etp.server.web.controller.proxy.request.*;
import com.xiaoniucode.etp.server.web.controller.proxy.response.HttpProxyDTO;
import com.xiaoniucode.etp.server.web.controller.proxy.response.TcpProxyDTO;
import jakarta.validation.constraints.NotNull;

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
    void deleteProxy(String id);

    /**
     * 根据 ID 查询tcp代理
     */
    TcpProxyDTO getTcpProxyById(String id);

    HttpProxyDTO getHttpProxyById(String id);

    void batchDeleteProxies(BatchDeleteRequest request);

    void setProxyStatus(String id,Integer status);

    List<TcpProxyDTO> getTcpProxies(String keyword, int page, int size);

    List<HttpProxyDTO> getHttpProxies(String keyword, int page, int size);
}