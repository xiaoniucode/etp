/*
 *    Copyright 2026 xiaoniucode
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http:
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.xiaoniucode.etp.server.web.service.impl;

import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.server.registry.ProxyManager;
import com.xiaoniucode.etp.server.web.dto.proxy.HttpProxyDTO;
import com.xiaoniucode.etp.server.web.dto.proxy.TcpProxyDTO;
import com.xiaoniucode.etp.server.web.entity.ProxyDO;
import com.xiaoniucode.etp.server.web.param.proxy.*;
import com.xiaoniucode.etp.server.web.repository.ProxyDomainRepository;
import com.xiaoniucode.etp.server.web.repository.ProxyRepository;
import com.xiaoniucode.etp.server.web.repository.ProxyTargetRepository;
import com.xiaoniucode.etp.server.web.service.ProxyService;
import com.xiaoniucode.etp.server.web.service.converter.ProxyConvert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProxyServiceImpl implements ProxyService {
    @Autowired
    private ProxyRepository proxyRepository;
    @Autowired
    private ProxyDomainRepository proxyDomainRepository;
    @Autowired
    private ProxyTargetRepository proxyTargetRepository;
    @Autowired
    private ProxyConvert proxyConvert;
    @Autowired
    private ProxyManager proxyManager;

    @Override
    public void createTcpProxy(TcpProxyCreateParam param) {
    }

    @Override
    public void createHttpProxy(HttpProxyCreateParam param) {
        ProxyDO proxyDO = proxyConvert.httpToEntity(param);
        proxyDO.setProtocol(ProtocolType.HTTP);
       proxyDO.setId("test");
        proxyRepository.save(proxyDO);
        System.out.println(proxyDO);
    }

    @Override
    public void updateTcpProxy(TcpProxyUpdateParam param) {
    }

    @Override
    public void updateHttpProxy(HttpProxyUpdateParam param) {
    }

    @Override
    public void deleteProxy(String id) {
    }

    @Override
    public TcpProxyDTO getTcpProxyById(String id) {
        return null;
    }

    @Override
    public HttpProxyDTO getHttpProxyById(String id) {
        return null;
    }

    @Override
    public void batchDeleteProxies(BatchDeleteParam param) {
    }

    @Override
    public void setProxyStatus(String id, Integer status) {
    }

    @Override
    public List<TcpProxyDTO> getTcpProxies(String keyword, int page, int size) {
        return List.of();
    }

    @Override
    public List<HttpProxyDTO> getHttpProxies(String keyword, int page, int size) {
        return List.of();
    }
}
