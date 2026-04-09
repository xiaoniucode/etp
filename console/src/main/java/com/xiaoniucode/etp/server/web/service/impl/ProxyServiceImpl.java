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

import com.xiaoniucode.etp.common.utils.StringUtils;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.registry.ProxyManager;
import com.xiaoniucode.etp.server.web.common.BizException;
import com.xiaoniucode.etp.server.web.dto.proxy.HttpProxyListDTO;
import com.xiaoniucode.etp.server.web.dto.proxy.HttpProxyDetailDTO;
import com.xiaoniucode.etp.server.web.dto.proxy.TcpProxyListDTO;
import com.xiaoniucode.etp.server.web.dto.proxy.TcpProxyDetailDTO;
import com.xiaoniucode.etp.server.web.entity.*;
import com.xiaoniucode.etp.server.web.param.loadbalance.LoadBalanceParam;
import com.xiaoniucode.etp.server.web.param.proxy.*;
import com.xiaoniucode.etp.server.web.repository.*;
import com.xiaoniucode.etp.server.web.service.ProxyService;
import com.xiaoniucode.etp.server.web.service.converter.*;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProxyServiceImpl implements ProxyService {
    private final Logger logger = LoggerFactory.getLogger(ProxyServiceImpl.class);
    @Autowired
    private ProxyRepository proxyRepository;
    @Autowired
    private ProxyDomainRepository proxyDomainRepository;
    @Autowired
    private ProxyTargetRepository proxyTargetRepository;
    @Autowired
    private HttpProxyRepository httpProxyRepository;
    @Autowired
    private BandwidthRepository bandwidthRepository;
    @Autowired
    private LoadBalanceRepository loadBalanceRepository;
    @Autowired
    private TransportRepository transportRepository;
    @Autowired
    private AgentRepository agentRepository;
    @Autowired
    private ProxyConvert proxyConvert;
    @Autowired
    private ProxyDomainConvert proxyDomainConvert;
    @Autowired
    private ProxyTargetConvert proxyTargetConvert;
    @Autowired
    private TransportConvert transportConvert;
    @Autowired
    private LoadBalanceConvert loadBalanceConvert;
    @Autowired
    private BandwidthConvert bandwidthConvert;
    @Autowired
    private ProxyManager proxyManager;
    @Resource
    private AppConfig appConfig;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createHttpProxy(HttpProxyCreateParam param) {

        String proxyId = UUID.randomUUID().toString();
        //1.基础信息
        if (proxyRepository.existsByAgentIdAndName(param.getAgentId(), param.getName())) {
            throw new BizException("该客户端下已存在同名代理名称: " + param.getName());
        }
        ProxyDO proxyDO = proxyConvert.toDO(param, proxyId);
        proxyRepository.save(proxyDO);
        //2.HTTP特有
        HttpProxyDO httpDO = proxyConvert.toHttpDO(param, proxyId);
        httpProxyRepository.save(httpDO);
        //3.服务列表
        if (param.getDeploymentMode().isStandalone() && param.getTargets().size() > 1) {
            throw new BizException("单机服务只能配置一个目标节点");
        }
        List<ProxyTargetDO> proxyTargetDOList = proxyTargetConvert.toDOList(param.getTargets(), proxyId);
        proxyTargetRepository.saveAll(proxyTargetDOList);
        //4.负载均衡
        //如果目标服务是集群部署，需要保存负载均衡相关配置
        if (param.getDeploymentMode().isCluster()) {
            LoadBalanceDO loadBalanceDO = loadBalanceConvert.toDO(param.getLoadBalance(), proxyId);
            loadBalanceRepository.save(loadBalanceDO);
        }
        //5.带宽
        BandwidthDO bandwidthDO = bandwidthConvert.toDO(param.getBandwidth(), proxyId);
        bandwidthRepository.save(bandwidthDO);
        //6.传输
        TransportDO transportDO = transportConvert.toDO(param.getTransport(), proxyId);
        transportRepository.save(transportDO);
        //7.HTTP域名信息
        List<HttpProxyDomainDO> httpProxyDomainList = proxyDomainConvert.toDOList(param.getDomains(), proxyId);
        proxyDomainRepository.saveAll(httpProxyDomainList);
        logger.debug("HTTP代理创建成功：{}", proxyDO.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateHttpProxy(HttpProxyUpdateParam param) {
        String proxyId = param.getId();
        ProxyDO proxyDO = proxyRepository.findById(proxyId).orElseThrow(() -> new BizException("代理配置不存在"));
        if (proxyRepository.existsByAgentIdAndNameAndIdNot(
                proxyDO.getAgentId(), param.getName(), proxyId)) {
            throw new BizException("该客户端下已存在同名代理名称: " + param.getName());
        }
        //1.基本信息
        proxyConvert.updateDO(param, proxyDO);
        proxyRepository.save(proxyDO);

        //2.HTTP特有
        HttpProxyDO httpProxyDO = httpProxyRepository.findById(proxyId)
                .orElseGet(() -> {
                    HttpProxyDO newHttp = new HttpProxyDO();
                    newHttp.setProxyId(proxyId);
                    return newHttp;
                });
        proxyConvert.updateHttpDO(param, httpProxyDO);
        httpProxyRepository.save(httpProxyDO);
        //3.服务列表
        if (param.getDeploymentMode().isStandalone() && param.getTargets().size() > 1) {
            throw new BizException("单机服务只能配置一个目标节点");
        }
        // 先删除旧的 Targets
        proxyTargetRepository.deleteByProxyId(proxyId);
        // 再插入新的
        if (!CollectionUtils.isEmpty(param.getTargets())) {
            List<ProxyTargetDO> targetList = proxyTargetConvert.toDOList(param.getTargets(), proxyId);
            proxyTargetRepository.saveAll(targetList);
        }
        //4.负载均衡
        if (param.getDeploymentMode().isCluster()) {
            LoadBalanceParam loadBalanceParam = param.getLoadBalance();

            LoadBalanceDO loadBalanceDO = loadBalanceRepository.findById(proxyId)
                    .orElseGet(() -> {
                        LoadBalanceDO newLB = new LoadBalanceDO();
                        newLB.setProxyId(proxyId);
                        return newLB;
                    });
            loadBalanceConvert.updateDO(loadBalanceParam, loadBalanceDO);
            loadBalanceRepository.save(loadBalanceDO);
        } else {
            //单机服务 尝试删除负载均衡配置
            loadBalanceRepository.deleteById(proxyId);
        }

        //5.带宽
        bandwidthRepository.deleteById(proxyId);
        BandwidthDO bandwidthDO = bandwidthConvert.toDO(param.getBandwidth(), proxyId);
        bandwidthRepository.save(bandwidthDO);
        //6.传输
        transportRepository.deleteById(proxyId);
        TransportDO transportDO = transportConvert.toDO(param.getTransport(), proxyId);
        transportRepository.save(transportDO);
        //7.HTTP域名信息
        proxyDomainRepository.deleteByProxyId(proxyId);
        if (!CollectionUtils.isEmpty(param.getDomains())) {
            List<HttpProxyDomainDO> domainList = proxyDomainConvert.toDOList(param.getDomains(), proxyId);
            proxyDomainRepository.saveAll(domainList);
        }
        logger.debug("HTTP代理更新成功：{}", proxyDO.getName());
    }

    /**
     * 查询 HTTP 代理列表
     *
     * @param keyword 关键字 按名称模糊查询
     * @param page    当前页（从 0 开始）
     * @param size    每页大小
     * @return HTTP 代理列表
     */
    @Override
    public List<HttpProxyListDTO> getHttpProxies(String keyword, int page, int size) {
        int currentPage = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(currentPage, size, Sort.by(Sort.Direction.DESC, "updatedAt"));

        Page<ProxyDO> proxyPage;
        if (!StringUtils.hasText(keyword)) {
            proxyPage = proxyRepository.findAll(pageable);
        } else {
            proxyPage = proxyRepository.findHttpProxiesByKeyword(keyword.trim(), pageable);
        }

        if (proxyPage.isEmpty()) {
            return List.of();
        }

        List<ProxyDO> proxyList = proxyPage.getContent();
        List<String> proxyIds = proxyList.stream().map(ProxyDO::getId).toList();
        List<String> agentIds = proxyList.stream().map(ProxyDO::getAgentId).toList();

        Map<String, AgentDO> agentMap = agentRepository.findByIdIn(agentIds)
                .stream()
                .collect(Collectors.toMap(
                        AgentDO::getId,
                        agent -> agent,
                        (existing, replacement) -> existing
                ));

        Map<String, List<String>> domainsMap = proxyDomainRepository.findByProxyIdIn(proxyIds)
                .stream()
                .collect(Collectors.groupingBy(
                        HttpProxyDomainDO::getProxyId,
                        Collectors.mapping(HttpProxyDomainDO::getDomain, Collectors.toList())
                ));

        int httpProxyPort = appConfig.getHttpProxyPort();

        return proxyList.stream()
                .map(proxyDO -> {
                    HttpProxyListDTO httpDTO = proxyConvert.toHttpDTO(proxyDO, httpProxyPort);

                    AgentDO agentDO = agentMap.get(proxyDO.getAgentId());
                    if (agentDO != null && agentDO.getAgentType() != null) {
                        httpDTO.setAgentType(agentDO.getAgentType().getCode());
                    }

                    httpDTO.setDomains(domainsMap.getOrDefault(proxyDO.getId(), Collections.emptyList()));

                    return httpDTO;
                })
                .collect(Collectors.toList());
    }

    @Override
    public HttpProxyDetailDTO getHttpProxyById(String id) {
        return null;
    }

    @Override
    public void createTcpProxy(TcpProxyCreateParam param) {
    }


    @Override
    public void updateTcpProxy(TcpProxyUpdateParam param) {
    }

    @Override
    public TcpProxyDetailDTO getTcpProxyById(String id) {
        return null;
    }

    @Override
    public List<TcpProxyListDTO> getTcpProxies(String keyword, int page, int size) {
        return List.of();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProxy(String id) {
        // 验证代理是否存在
        ProxyDO proxyDO = proxyRepository.findById(id).orElseThrow(() -> new BizException("代理配置不存在"));
        //todo kernel
        proxyTargetRepository.deleteByProxyId(id);
        loadBalanceRepository.deleteById(id);
        bandwidthRepository.deleteById(id);
        transportRepository.deleteById(id);
        proxyDomainRepository.deleteByProxyId(id);
        httpProxyRepository.deleteById(id);
        proxyRepository.deleteById(id);

        //todo BasicAuth

        //todo IP CIDR

        logger.debug("代理删除成功：{}", proxyDO.getName());
    }

    @Override
    public void batchDeleteProxies(ProxyBatchDeleteParam param) {
    }

    @Override
    public void setProxyStatus(String id, Integer status) {

    }


}
