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

import com.xiaoniucode.etp.common.message.PageResult;
import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.service.repository.AgentQueryRepository;
import com.xiaoniucode.etp.server.service.repository.ProxyQueryRepository;
import com.xiaoniucode.etp.server.web.dto.proxy.embedded.TunnelDetailDTO;
import com.xiaoniucode.etp.server.web.dto.proxy.embedded.TunnelListDTO;
import com.xiaoniucode.etp.server.web.service.EmbeddedService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class EmbeddedServiceImpl implements EmbeddedService {
    @Autowired
    @Qualifier("embeddedAgentQueryRepository")
    private AgentQueryRepository agentQueryRepository;
    @Autowired
    @Qualifier("embeddedProxyQueryRepository")
    private ProxyQueryRepository proxyQueryRepository;
    @Resource
    private AppConfig appConfig;

    @Override
    public PageResult<TunnelListDTO> listByPage(int page, int size) {
        int httpProxyPort = appConfig.getHttpProxyPort();
        PageResult<ProxyConfig> res = proxyQueryRepository.findByPage(page, size);
        List<ProxyConfig> records = res.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PageResult<>(List.of(), 0L, page, size);
        }
        List<TunnelListDTO> tunnelList = new ArrayList<>();
        records.forEach(config -> {
            TunnelListDTO tunnelListDTO = new TunnelListDTO();
            tunnelListDTO.setHttpProxyPort(httpProxyPort);
            if (config.isTcp()) {
                TunnelListDTO.TcpTunnelListDTO tcpTunnelListDTO = new TunnelListDTO.TcpTunnelListDTO();
                tcpTunnelListDTO.setProxyId(config.getProxyId());
                tcpTunnelListDTO.setProtocol(config.getProtocol().getCode());
                tcpTunnelListDTO.setName(config.getName());
                tcpTunnelListDTO.setStatus(config.getStatus().getCode());
                tcpTunnelListDTO.setListenPort(config.getListenPort());
                tunnelListDTO.setTunnel(tcpTunnelListDTO);
            } else if (config.isHttp()) {
                TunnelListDTO.HttpTunnelListDTO httpTunnelListDTO = new TunnelListDTO.HttpTunnelListDTO();
                httpTunnelListDTO.setProxyId(config.getProxyId());
                httpTunnelListDTO.setProtocol(config.getProtocol().getCode());
                httpTunnelListDTO.setName(config.getName());
                httpTunnelListDTO.setStatus(config.getStatus().getCode());
                Set<String> domains = proxyQueryRepository.findDomainsByProxyId(config.getProxyId());
                httpTunnelListDTO.setDomains(new ArrayList<>(domains));
                tunnelListDTO.setTunnel(httpTunnelListDTO);
            }
            tunnelList.add(tunnelListDTO);
        });
        return new PageResult<>(tunnelList, res.getTotal(), page, size);
    }

    @Override
    public TunnelDetailDTO detail(String proxyId) {

        return null;
    }

    @Override
    public void delete(String proxyId) {

    }
}