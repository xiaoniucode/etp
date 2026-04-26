///*
// *    Copyright 2026 xiaoniucode
// *
// *    Licensed under the Apache License, Version 2.0 (the "License");
// *    you may not use this file except in compliance with the License.
// *    You may obtain a copy of the License at
// *
// *        http://www.apache.org/licenses/LICENSE-2.0
// *
// *    Unless required by applicable law or agreed to in writing, software
// *    distributed under the License is distributed on an "AS IS" BASIS,
// *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *    See the License for the specific language governing permissions and
// *    limitations under the License.
// */
//
//package com.xiaoniucode.etp.server.manager;
//
//import com.xiaoniucode.etp.core.domain.ProxyConfig;
//import com.xiaoniucode.etp.server.exceptions.EtpException;
//import com.xiaoniucode.etp.server.metrics.MetricsCollector;
//import com.xiaoniucode.etp.server.port.PortAcceptor;
//import com.xiaoniucode.etp.server.port.PortManager;
//import com.xiaoniucode.etp.server.security.IpAccessChecker;
//import com.xiaoniucode.etp.server.statemachine.stream.StreamManager;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * 运行时代理管理器，负责代理的注册、注销和状态变更等操作。
// */
//@Component
//public class ProxyManager {
//    private Map<String/*proxyId*/, ProxyConfig> proxyRegistry = new ConcurrentHashMap<>();
//    private Map<String/*agentId*/, List<String/*proxyId*/>> agentProxyIndex = new ConcurrentHashMap<>();
//    private Map<Integer/*listenPort*/, String/*proxyId*/> portProxyIndex = new ConcurrentHashMap<>();
//    private Map<String/*domain*/, String/*proxyId*/> domainProxyIndex = new ConcurrentHashMap<>();
//
//    @Autowired
//    private MetricsCollector metricsCollector;
//    @Autowired
//    private IpAccessChecker ipAccessChecker;
//    @Autowired
//    private PortAcceptor portAcceptor;
//    @Autowired
//    private PortManager portManager;
//    @Autowired
//    private StreamManager streamManager;
//
//    public void register(ProxyConfig config) throws EtpException {
//        String proxyId = config.getProxyId();
//        String agentId = config.getAgentId();
//        proxyRegistry.put(proxyId, config);
//
//        if (config.isTcp()) {
//            portAcceptor.bindPort(config.getListenPort());
//        } else if (config.isTcp()) {
//
//        }
//    }
//
//    public void unregister(String proxyId) throws EtpException {
//        ProxyConfig config = proxyRegistry.remove(proxyId);
//        if (config == null) {
//            return;
//        }
//        if (config.isTcp()) {
//            closeCacheByPort(config.getListenPort());
//        } else if (config.isHttp()) {
////            for (int i = 0; i < 10; i++) {
////                streamManager.fireCloseByDomain(domainBinding.getDomain());
////            }
//
//        }
//        //删除IP访问控制
//        ipAccessChecker.invalidate(proxyId);
//        //删除代理流量统计记录
//        metricsCollector.removeByProxyId(proxyId);
//    }
//
//
//    public void reregister(ProxyConfig oldConfig,ProxyConfig newConfig) throws EtpException {
//
//    }
//
//    private void closeCacheByPort(int listenPort) {
//        portManager.release(listenPort);
//        portAcceptor.stopPortListen(listenPort);
//        streamManager.fireCloseByPort(listenPort);
//    }
//
//    public void unregisterAll(String agentId) throws EtpException {
//
//    }
//
//    public void changeStatus(String proxyId, boolean enabled) throws EtpException {
//        ProxyConfig config = proxyRegistry.get(proxyId);
//        if (config.isTcp()) {
//            Integer listenPort = config.getListenPort();
//            if (enabled && !config.getStatus().isOpen()) {
//                portAcceptor.bindPort(listenPort);
//            } else {
//                portAcceptor.stopPortListen(listenPort);
//                streamManager.fireCloseByPort(listenPort);
//            }
//        }
//
//    }
//
//    public boolean exist(String proxyId) {
//        return proxyRegistry.containsKey(proxyId);
//    }
//}