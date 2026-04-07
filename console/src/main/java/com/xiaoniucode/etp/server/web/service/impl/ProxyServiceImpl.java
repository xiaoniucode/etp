/*
 *    Copyright 2026 xiaoniucode
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.xiaoniucode.etp.server.web.service.impl;

import com.xiaoniucode.etp.server.web.controller.proxy.request.*;
import com.xiaoniucode.etp.server.web.controller.proxy.response.HttpProxyDTO;
import com.xiaoniucode.etp.server.web.controller.proxy.response.TcpProxyDTO;
import com.xiaoniucode.etp.server.web.repository.ProxyRepository;
import com.xiaoniucode.etp.server.web.service.ProxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProxyServiceImpl implements ProxyService {
    @Autowired
    private ProxyRepository proxyRepository;
    @Override
    public void createTcpProxy(TcpProxyCreateRequest proxy) {

    }

    @Override
    public void createHttpProxy(HttpProxyCreateRequest proxy) {

    }

    @Override
    public void updateTcpProxy(TcpProxyUpdateRequest request) {

    }

    @Override
    public void updateHttpProxy(HttpProxyUpdateRequest request) {

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
    public void batchDeleteProxies(BatchDeleteRequest request) {

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
