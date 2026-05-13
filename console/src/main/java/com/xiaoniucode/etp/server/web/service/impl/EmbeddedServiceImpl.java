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
import com.xiaoniucode.etp.core.domain.*;
import com.xiaoniucode.etp.core.enums.DeploymentMode;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.service.repository.AgentQueryRepository;
import com.xiaoniucode.etp.server.service.repository.ProxyQueryRepository;
import com.xiaoniucode.etp.server.statemachine.agent.AgentInfo;
import com.xiaoniucode.etp.server.statemachine.agent.AgentManager;
import com.xiaoniucode.etp.server.vhost.DomainInfo;
import com.xiaoniucode.etp.server.web.common.exception.BizException;
import com.xiaoniucode.etp.server.web.common.message.PageQuery;
import com.xiaoniucode.etp.server.web.dto.accesscontrol.AccessControlDetailDTO;
import com.xiaoniucode.etp.server.web.dto.basicauth.BasicAuthDetailDTO;
import com.xiaoniucode.etp.server.web.dto.proxy.embedded.TunnelDetailDTO;
import com.xiaoniucode.etp.server.web.dto.proxy.embedded.TunnelListDTO;
import com.xiaoniucode.etp.server.web.service.EmbeddedService;
import com.xiaoniucode.etp.server.web.service.converter.*;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EmbeddedServiceImpl implements EmbeddedService {
    @Autowired
    @Qualifier("embeddedAgentQueryRepository")
    private AgentQueryRepository agentQueryRepository;
    @Autowired
    @Qualifier("embeddedProxyQueryRepository")
    private ProxyQueryRepository proxyQueryRepository;
    @Autowired
    private AgentConvert agentConvert;
    @Autowired
    private ProxyConvert proxyConvert;
    @Autowired
    private BasicAuthConvert basicAuthConvert;
    @Autowired
    private TransportConvert transportConvert;
    @Autowired
    private AccessControlConvert accessControlConvert;
    @Resource
    private AppConfig appConfig;
    @Autowired
    private AgentManager agentManager;

    @Override
    public PageResult<TunnelListDTO> listByPage(PageQuery pageQuery) {
        int httpProxyPort = appConfig.getHttpProxyPort();
        PageResult<ProxyConfig> res = proxyQueryRepository.findByPage(pageQuery.getCurrent(), pageQuery.getSize());
        List<ProxyConfig> records = res.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PageResult<>(List.of(), 0L, pageQuery.getCurrent(), pageQuery.getSize());
        }
        List<TunnelListDTO> tunnelList = new ArrayList<>();
        records.forEach(config -> {
            TunnelListDTO tunnelListDTO = new TunnelListDTO();
            tunnelListDTO.setHttpProxyPort(httpProxyPort);
            if (config.isTcp()) {
                tunnelListDTO.setTunnel(proxyConvert.toTcpDTOList(config));
            } else if (config.isHttp()) {
                TunnelListDTO.HttpTunnelListDTO httpDTOList = proxyConvert.toHttpDTOList(config);
                Set<DomainInfo> domainInfos = proxyQueryRepository.findDomainsByProxyId(config.getProxyId());
                List<String> fullDomains = domainInfos.stream().map(DomainInfo::getFullDomain).toList();
                httpDTOList.setDomains(fullDomains);
                tunnelListDTO.setTunnel(httpDTOList);
            }
            tunnelList.add(tunnelListDTO);
        });
        return new PageResult<>(tunnelList, res.getTotal(), pageQuery.getCurrent(), pageQuery.getSize());
    }

    @Override
    public TunnelDetailDTO detail(String proxyId) {
        Optional<ProxyConfig> configOpt = proxyQueryRepository.findById(proxyId);
        if (configOpt.isEmpty()) {
            throw new BizException("不存在");
        }
        ProxyConfig config = configOpt.get();
        String agentId = config.getAgentId();
        Optional<AgentInfo> agentOpt = agentQueryRepository.findById(agentId);
        if (agentOpt.isEmpty()) {
            throw new BizException("获取详情失败");
        }
        TunnelDetailDTO tunnelDetailDTO = new TunnelDetailDTO();
        tunnelDetailDTO.setAgent(agentConvert.toDTO(agentOpt.get()));
        tunnelDetailDTO.setHttpProxyPort(appConfig.getHttpProxyPort());
        int deploymentMode = config.getTargets().size() > 1 ? DeploymentMode.CLUSTER.getCode() : DeploymentMode.STANDALONE.getCode();

        AccessControlDetailDTO accessControlDetailDTO = accessControlConvert.toDetailDTO(config.getAccessControl());
        if (config.isHttp()) {
            TunnelDetailDTO.HttpProxyDTO httpProxyDTO = proxyConvert.toHttpProxyDTO(config);
            httpProxyDTO.setDeploymentMode(deploymentMode);
            Set<DomainInfo> domainInfos = proxyQueryRepository.findDomainsByProxyId(proxyId);
            Set<String> fullDomains = domainInfos.stream().map(DomainInfo::getFullDomain).collect(Collectors.toSet());
            httpProxyDTO.setDomains(fullDomains);
            httpProxyDTO.setDomainType(config.getRouteConfig().getDomainType().getCode());
            httpProxyDTO.setTransport(transportConvert.toDTO(config.getTransport()));
            BasicAuthConfig basicAuth = config.getBasicAuth();
            if (basicAuth != null) {
                BasicAuthDetailDTO basicAuthDetailDTO = basicAuthConvert.toDetailDTO(basicAuth, basicAuth.getUsers());
                httpProxyDTO.setBasicAuth(basicAuthDetailDTO);
            }
            httpProxyDTO.setAccessControl(accessControlDetailDTO);
            tunnelDetailDTO.setProxy(httpProxyDTO);
        } else if (config.isTcp()) {
            TunnelDetailDTO.TcpProxyDTO tcpProxyDTO = proxyConvert.toTcpProxyDTO(config);
            tcpProxyDTO.setDeploymentMode(deploymentMode);
            tcpProxyDTO.setTransport(transportConvert.toDTO(config.getTransport()));
            tcpProxyDTO.setAccessControl(accessControlDetailDTO);
            tunnelDetailDTO.setProxy(tcpProxyDTO);
        }

        return tunnelDetailDTO;
    }

    @Override
    public void batchDelete(List<String> agentIds) {
        if (CollectionUtils.isEmpty(agentIds)) {
            return;
        }
        agentIds.forEach(agentId -> agentManager.kickout(agentId));
    }
}