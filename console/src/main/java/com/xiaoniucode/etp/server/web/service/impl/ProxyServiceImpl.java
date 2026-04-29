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

import com.baidu.fsg.uid.UidGenerator;
import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.enums.*;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.manager.ProxyManager;
import com.xiaoniucode.etp.server.vhost.DomainGenerator;
import com.xiaoniucode.etp.server.web.common.message.PageResult;
import com.xiaoniucode.etp.server.web.common.exception.BizException;
import com.xiaoniucode.etp.server.web.dto.loadbalance.LoadBalanceDTO;
import com.xiaoniucode.etp.server.web.dto.proxy.*;
import com.xiaoniucode.etp.server.web.dto.transport.TransportDTO;
import com.xiaoniucode.etp.server.web.entity.*;
import com.xiaoniucode.etp.server.web.param.bandwidth.BandwidthSaveParam;
import com.xiaoniucode.etp.server.web.param.loadbalance.LoadBalanceParam;
import com.xiaoniucode.etp.server.web.param.proxy.*;
import com.xiaoniucode.etp.server.web.repository.*;
import com.xiaoniucode.etp.server.web.service.ProxyService;
import com.xiaoniucode.etp.server.web.service.assembler.ProxyAssembler;
import com.xiaoniucode.etp.server.web.service.converter.*;
import com.xiaoniucode.etp.server.web.support.tx.TransactionHelper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
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
    private LoadBalanceRepository loadBalanceRepository;
    @Autowired
    private TransportRepository transportRepository;
    @Autowired
    private BasicAuthRepository basicAuthRepository;
    @Autowired
    private BasicUserRepository basicUserRepository;
    @Autowired
    private AccessControlRepository accessControlRepository;
    @Autowired
    private AccessControlRuleRepository accessControlRuleRepository;

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
    private ProxyAssembler proxyAssembler;
    @Resource
    private AppConfig appConfig;
    @Autowired
    private UidGenerator uidGenerator;
    @Autowired
    private DomainGenerator domainGenerator;
    @Autowired
    private ProxyManager proxyManager;
    @Autowired
    private TransactionHelper transactionHelper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createHttpProxy(HttpProxyCreateParam param) {
        //1.基础信息
        if (proxyRepository.existsByAgentIdAndName(param.getAgentId(), param.getName())) {
            throw new BizException("该客户端下已存在同名代理名称: " + param.getName());
        }

        //生成代理配置信息
        String proxyId = uidGenerator.getUIDAsString();

        ProxyDO proxyDO = proxyConvert.toDO(param, proxyId);
        proxyDO.setSourceType(ProxySourceType.MANUAL);
        BandwidthSaveParam bandwidth = param.getBandwidth();
        if (bandwidth != null) {

        }
        proxyRepository.save(proxyDO);
        //3.服务列表
        if (proxyDO.getDeploymentMode().isStandalone() && param.getTargets().size() > 1) {
            throw new BizException("单机服务只能配置一个目标节点");
        }
        List<ProxyTargetDO> proxyTargetDOList = proxyTargetConvert.toDOList(param.getTargets(), proxyId);
        proxyTargetRepository.saveAll(proxyTargetDOList);
        //4.负载均衡
        //如果目标服务是集群部署，需要保存负载均衡相关配置
        if (proxyDO.getDeploymentMode().isCluster()) {
            LoadBalanceDO loadBalanceDO = loadBalanceConvert.toDO(param.getLoadBalance(), proxyId);
            loadBalanceRepository.save(loadBalanceDO);
        }
        //6.传输
        TransportDO transportDO = transportConvert.toDO(param.getTransport(), proxyId);
        transportRepository.save(transportDO);
        //7.HTTP域名信息
        String baseDomain = appConfig.getBaseDomain();
        DomainType domainType = proxyDO.getDomainType();
        if (domainType.isAuto()) {
            String domain = domainGenerator.generateSubdomain(baseDomain);
            proxyDomainRepository.save(new ProxyDomainDO(proxyId, domain, baseDomain, domainType));
        } else if (domainType.isCustomDomain()) {
            Set<String> domains = param.getDomains();
            List<ProxyDomainDO> existsList = proxyDomainRepository.findByFullDomainIn(domains);
            if (!existsList.isEmpty()) {
                String existDomains = existsList.stream()
                        .map(ProxyDomainDO::getDomain)
                        .collect(Collectors.joining(", "));
                throw new BizException("以下域名已被使用: " + existDomains);
            }
            List<ProxyDomainDO> list = domains.stream()
                    .map(domain -> new ProxyDomainDO(
                            proxyId,
                            domain,
                            null,
                            domainType
                    ))
                    .toList();
            proxyDomainRepository.saveAll(list);
        } else if (domainType.isSubdomain()) {
            Set<String> prefixes = param.getDomains();
            List<String> fullDomains = prefixes.stream()
                    .map(prefix -> prefix + "." + baseDomain)
                    .toList();
            List<ProxyDomainDO> existsList = proxyDomainRepository.findByFullDomainIn(fullDomains);
            if (!existsList.isEmpty()) {
                String existDomains = existsList.stream()
                        .map(ProxyDomainDO::getDomain)
                        .collect(Collectors.joining(", "));
                throw new BizException("以下子域名已被使用: " + existDomains);
            }
            List<ProxyDomainDO> list = prefixes.stream()
                    .map(prefix -> new ProxyDomainDO(
                            proxyId,
                            prefix,
                            baseDomain,
                            domainType
                    ))
                    .toList();
            proxyDomainRepository.saveAll(list);
        }

        //init access control
        AccessControlDO accessControlDO = new AccessControlDO();
        accessControlDO.setProxyId(proxyId);
        accessControlDO.setMode(AccessControl.DENY);
        accessControlDO.setEnabled(false);
        accessControlRepository.save(accessControlDO);
        //init http basic auth
        BasicAuthDO basicAuthDO = new BasicAuthDO();
        basicAuthDO.setEnabled(false);
        basicAuthDO.setProxyId(proxyId);
        basicAuthRepository.save(basicAuthDO);

        if (proxyDO.getStatus().isOpen()) {
            transactionHelper.afterCommit(() ->
                    proxyManager.activate(proxyAssembler.toProxyConfig(proxyDO)));
        }

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
        proxyDO.setSourceType(ProxySourceType.MANUAL);
        proxyRepository.save(proxyDO);

        //3.服务列表
        if (proxyDO.getDeploymentMode().isStandalone() && param.getTargets().size() > 1) {
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
        if (proxyDO.getDeploymentMode().isCluster()) {
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
        //6.传输
        transportRepository.deleteById(proxyId);
        TransportDO transportDO = transportConvert.toDO(param.getTransport(), proxyId);
        transportRepository.save(transportDO);
        //todo 7.HTTP域名信息
        proxyDomainRepository.deleteByProxyId(proxyId);

//        if (!CollectionUtils.isEmpty(boundDomains)) {
//            List<HttpProxyDomainDO> domainList = proxyDomainConvert.toDOList(boundDomains, proxyId);
//            proxyDomainRepository.saveAll(domainList);
//        }
        transactionHelper.afterCommit(() ->
                proxyManager.reconcile(proxyAssembler.toProxyConfig(proxyDO)));
        logger.debug("HTTP代理更新成功：{}", proxyDO.getName());
    }

    /**
     * 查询 HTTP 代理列表
     *
     * @param keyword 关键字
     * @param page    当前页（从 0 开始）
     * @param size    每页大小
     * @return HTTP 代理列表
     */
    @Override
    public PageResult<HttpProxyListDTO> getHttpProxies(String keyword, int page, int size) {
        int currentPage = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(currentPage, size);

        String queryKey = null;
        if (StringUtils.hasText(keyword)) {
            if (keyword.matches("\\d{19}")) {
                queryKey = keyword.trim();
            } else {
                queryKey = "%" + keyword.trim() + "%";
            }
        }

        Page<Object[]> resultPage = proxyRepository.findProxiesWithAssociations(queryKey, ProtocolType.HTTP, pageable);

        if (resultPage.isEmpty()) {
            return PageResult.empty(page, size);
        }
        int httpProxyPort = appConfig.getHttpProxyPort();
        List<Object[]> content = resultPage.getContent();

        List<String> proxyIds = content.stream()
                .map(row -> (ProxyDO) row[0])
                .map(ProxyDO::getId)
                .collect(Collectors.toList());
        Map<String, List<String>> domainsMap = proxyDomainRepository.findByProxyIdIn(proxyIds)
                .stream()
                .collect(Collectors.groupingBy(
                        ProxyDomainDO::getProxyId,
                        Collectors.mapping(ProxyDomainDO::getFullDomain, Collectors.toList())
                ));
        List<HttpProxyListDTO> res = new ArrayList<>();
        for (Object[] objects : content) {
            ProxyDO proxyDO = (ProxyDO) objects[0];
            AgentDO agentDO = (AgentDO) objects[1];
            HttpProxyListDTO httpDTO = proxyConvert.toHttpListDTO(proxyDO, httpProxyPort);

            if (agentDO != null && agentDO.getAgentType() != null) {
                httpDTO.setAgentType(agentDO.getAgentType().getCode());
            }
            httpDTO.setDomains(domainsMap.getOrDefault(proxyDO.getId(), Collections.emptyList()));
            httpDTO.setHttpProxyPort(httpProxyPort);
            res.add(httpDTO);
        }
        return PageResult.wrap(resultPage, res);
    }

    @Override
    public HttpProxyDetailDTO getHttpProxyById(String id) {
        ProxyDetailQueryResult detail = proxyRepository.findDetailByProxyId(id);
        AgentDO agentDO = detail.getAgentDO();
        ProxyDO proxyDO = detail.getProxyDO();
        TransportDO transportDO = detail.getTransportDO();
        LoadBalanceDO loadBalanceDO = detail.getLoadBalanceDO();

        List<ProxyTargetDO> proxyTargetDos = proxyTargetRepository.findByProxyId(id);
        List<ProxyDomainDO> httpProxyDomainDOs = proxyDomainRepository.findByProxyId(id);

        HttpProxyDetailDTO httpProxyDetailDTO = proxyConvert.toHttpDetailDTO(proxyDO, agentDO.getAgentType().getCode());
        TransportDTO transportDTO = transportConvert.toDTO(transportDO);

        List<TargetDTO> targetDTOList = proxyTargetConvert.toDTOList(proxyTargetDos);
        httpProxyDetailDTO.setTransport(transportDTO);

        if (proxyDO.getDeploymentMode().isCluster()) {
            LoadBalanceDTO loadBalanceDTO = loadBalanceConvert.toDTO(loadBalanceDO);
            httpProxyDetailDTO.setLoadBalance(loadBalanceDTO);
        }

        httpProxyDetailDTO.setTargets(targetDTOList);
        List<String> domains = httpProxyDomainDOs.stream().map(ProxyDomainDO::getDomain).toList();
        httpProxyDetailDTO.setDomains(domains);
        return httpProxyDetailDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createTcpProxy(TcpProxyCreateParam param) {
        String proxyId = uidGenerator.getUIDAsString();
        //1.基础信息
        if (proxyRepository.existsByAgentIdAndName(param.getAgentId(), param.getName())) {
            throw new BizException("该客户端下已存在同名代理名称: " + param.getName());
        }
        ProxyDO proxyDO = proxyConvert.toDO(param, proxyId);
        proxyDO.setListenPort(param.getRemotePort());
        proxyDO.setSourceType(ProxySourceType.MANUAL);
        proxyRepository.save(proxyDO);
        //3.服务列表
        if (proxyDO.getDeploymentMode().isStandalone() && param.getTargets().size() > 1) {
            throw new BizException("单机服务只能配置一个目标节点");
        }
        List<ProxyTargetDO> proxyTargetDOList = proxyTargetConvert.toDOList(param.getTargets(), proxyId);
        proxyTargetRepository.saveAll(proxyTargetDOList);
        //4.负载均衡
        //如果目标服务是集群部署，需要保存负载均衡相关配置
        if (proxyDO.getDeploymentMode().isCluster()) {
            LoadBalanceDO loadBalanceDO = loadBalanceConvert.toDO(param.getLoadBalance(), proxyId);
            loadBalanceRepository.save(loadBalanceDO);
        }
        //6.传输
        TransportDO transportDO = transportConvert.toDO(param.getTransport(), proxyId);
        transportRepository.save(transportDO);

        AccessControlDO accessControlDO = new AccessControlDO();
        accessControlDO.setProxyId(proxyId);
        accessControlDO.setMode(AccessControl.DENY);
        accessControlDO.setEnabled(false);
        accessControlRepository.save(accessControlDO);

        if (proxyDO.getStatus().isOpen()) {
            transactionHelper.afterCommit(() ->
                    proxyManager.activate(proxyAssembler.toProxyConfig(proxyDO)));
        }
        logger.debug("TCP 代理创建成功：{}", proxyDO.getName());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTcpProxy(TcpProxyUpdateParam param) {
        String proxyId = param.getId();
        ProxyDO proxyDO = proxyRepository.findById(proxyId).orElseThrow(() -> new BizException("代理配置不存在"));
        if (proxyRepository.existsByAgentIdAndNameAndIdNot(
                proxyDO.getAgentId(), param.getName(), proxyId)) {
            throw new BizException("该客户端下已存在同名代理名称: " + param.getName());
        }
        //1.基本信息
        proxyConvert.updateDO(param, proxyDO);
        proxyRepository.save(proxyDO);

        //3.服务列表
        if (proxyDO.getDeploymentMode().isStandalone() && param.getTargets().size() > 1) {
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
        if (proxyDO.getDeploymentMode().isCluster()) {
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

        //6.传输
        transportRepository.deleteById(proxyId);
        TransportDO transportDO = transportConvert.toDO(param.getTransport(), proxyId);
        transportRepository.save(transportDO);
        logger.debug("TCP 代理更新成功：{}", proxyDO.getName());
    }

    @Override
    public TcpProxyDetailDTO getTcpProxyById(String id) {
        ProxyDetailQueryResult detail = proxyRepository.findDetailByProxyId(id);
        AgentDO agentDO = detail.getAgentDO();
        ProxyDO proxyDO = detail.getProxyDO();
        TransportDO transportDO = detail.getTransportDO();
        LoadBalanceDO loadBalanceDO = detail.getLoadBalanceDO();

        List<ProxyTargetDO> proxyTargetDos = proxyTargetRepository.findByProxyId(id);

        TcpProxyDetailDTO tcpProxyDetailDTO = proxyConvert.toTcpDetailDTO(proxyDO, agentDO.getAgentType().getCode());
        TransportDTO transportDTO = transportConvert.toDTO(transportDO);

        List<TargetDTO> targetDTOList = proxyTargetConvert.toDTOList(proxyTargetDos);
        tcpProxyDetailDTO.setTransport(transportDTO);

        if (proxyDO.getDeploymentMode().isCluster()) {
            LoadBalanceDTO loadBalanceDTO = loadBalanceConvert.toDTO(loadBalanceDO);
            tcpProxyDetailDTO.setLoadBalance(loadBalanceDTO);
        }
        tcpProxyDetailDTO.setTargets(targetDTOList);
        return tcpProxyDetailDTO;
    }

    @Override
    public PageResult<TcpProxyListDTO> getTcpProxies(String keyword, int page, int size) {
        int currentPage = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(currentPage, size);
        String queryKey = null;
        if (StringUtils.hasText(keyword)) {
            if (keyword.matches("\\d{19}")) {
                queryKey = keyword.trim();
            } else {
                queryKey = "%" + keyword.trim() + "%";
            }
        }
        Page<Object[]> resultPage = proxyRepository.findProxiesWithAssociations(queryKey, ProtocolType.TCP, pageable);
        if (resultPage.isEmpty()) {
            return PageResult.empty(page, size);
        }
        List<Object[]> content = resultPage.getContent();
        List<TcpProxyListDTO> res = new ArrayList<>();
        for (Object[] objects : content) {
            ProxyDO proxyDO = (ProxyDO) objects[0];
            AgentDO agentDO = (AgentDO) objects[1];

            TcpProxyListDTO tcpListDTO = proxyConvert.toTcpListDTO(proxyDO);
            if (agentDO != null && agentDO.getAgentType() != null) {
                tcpListDTO.setAgentType(agentDO.getAgentType().getCode());
            }
            res.add(tcpListDTO);
        }
        return PageResult.wrap(resultPage, res);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteProxies(ProxyBatchDeleteParam param) {
        List<String> ids = param.getIds();
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }

        // 批量验证是否存在
        List<String> existingIds = proxyRepository.findAllById(ids)
                .stream()
                .map(ProxyDO::getId)
                .toList();

        List<String> notFoundIds = ids.stream()
                .filter(id -> !existingIds.contains(id))
                .toList();

        if (!notFoundIds.isEmpty()) {
            throw new BizException("以下代理不存在: " + notFoundIds);
        }
        //Common
        proxyTargetRepository.deleteByProxyIdIn(ids);
        loadBalanceRepository.deleteByProxyIdIn(ids);
        transportRepository.deleteByProxyIdIn(ids);
        //IP CIDR
        accessControlRepository.deleteByProxyIdIn(ids);
        accessControlRuleRepository.deleteByProxyIdIn(ids);
        //HTTP
        proxyDomainRepository.deleteByProxyIdIn(ids);
        basicAuthRepository.deleteByProxyIdIn(ids);
        basicUserRepository.deleteByProxyIdIn(ids);
        // Base
        proxyRepository.deleteByIdIn(ids);
        //清空运行时数据
        transactionHelper.afterCommit(() -> proxyManager.deactivates(ids));
        logger.debug("批量删除代理成功，数量: {}", ids.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setProxyStatus(String id, Integer status) {
        ProxyDO proxyDO = proxyRepository.findById(id).orElseThrow(() -> new BizException("代理配置信息不存在"));
        if (proxyDO.getStatus().getCode().equals(status)) {
            return;
        }
        proxyDO.setStatus(ProxyStatus.fromCode(status));
        proxyRepository.save(proxyDO);
    }
}
