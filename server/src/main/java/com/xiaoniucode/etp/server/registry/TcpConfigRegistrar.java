package com.xiaoniucode.etp.server.registry;

import com.xiaoniucode.etp.common.utils.StringUtils;
import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.enums.AgentType;
import com.xiaoniucode.etp.server.exceptions.EtpException;
import com.xiaoniucode.etp.server.exceptions.PortConflictException;
import com.xiaoniucode.etp.server.generator.UUIDGenerator;
import com.xiaoniucode.etp.server.metrics.MetricsCollector;
import com.xiaoniucode.etp.server.port.PortAcceptor;
import com.xiaoniucode.etp.server.port.PortManager;
import com.xiaoniucode.etp.server.statemachine.agent.AgentManager;
import com.xiaoniucode.etp.server.statemachine.stream.StreamManager;
import com.xiaoniucode.etp.server.store.ProxyStore;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.javers.core.diff.Diff;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class TcpConfigRegistrar implements ConfigRegistrar {
    private final InternalLogger logger = InternalLoggerFactory.getInstance(TcpConfigRegistrar.class);
    @Autowired
    private PortManager portManager;
    @Autowired
    private AgentManager agentManager;
    @Autowired
    private ProxyStore proxyStore;
    @Autowired
    private PortAcceptor portAcceptor;
    @Autowired
    private StreamManager streamManager;
    @Autowired
    private  UUIDGenerator uuidGenerator;
    @Override
    public boolean supports(ProxyConfig config) {
        return config.isTcp();
    }

    @Override
    public void validate(ProxyConfig config) throws EtpException {
        if (!StringUtils.hasText(config.getName())) {
            throw new EtpException("代理名不能为空！");
        }
    }

    @Override
    public void register(ProxyConfig config) throws EtpException {
        String proxyId = uuidGenerator.uuid32();
        config.setProxyId(proxyId);
        Integer listenPort = config.getRemotePort();
        if (listenPort != null && !portManager.isAvailable(listenPort)) {
            logger.error("端口已被占用: {}", listenPort);
            throw new PortConflictException(listenPort);
        }

        AgentType clientType = config.getAgentType();
        String name = config.getName();
        ProxyConfig exist = proxyStore.findByAgentIdAndName(config.getAgentId(), name);
        //处理临时客户端名称冲突
        if (exist != null && clientType == AgentType.BINARY) {
            throw new EtpException("代理配置名称不能重复");
        }
        if (listenPort == null) {
            listenPort = portManager.acquire();
        }
        if (listenPort == null) {
            throw new EtpException("无可用端口，请联系管理员");
        }
        //处理临时客户端名字重复
        if (exist != null && (clientType == AgentType.SESSION)) {
            config.setName(listenPort + "." + name);
        }
        config.setListenPort(listenPort);

        agentManager.getAgentContext(config.getAgentId()).ifPresent(agentContext -> {
            agentManager.addProxyContextIndex(config.getProxyId(), agentContext);
        });
        portAcceptor.bindPort(listenPort);
    }

    @Override
    public void reregister(ProxyConfig oldConfig, ProxyConfig newConfig, Diff diff) throws EtpException {
        newConfig.setProxyId(oldConfig.getProxyId());
        Integer oldRemotePort = oldConfig.getRemotePort();
        Integer newRemotePort = newConfig.getRemotePort();
        Integer oldListenPort = oldConfig.getListenPort();
        if (!Objects.equals(oldRemotePort, newRemotePort)) {
            if (newRemotePort == null) {
                Integer newListenPort = portManager.acquire();
                if (newListenPort == null) {
                    throw new EtpException("无可用端口，请联系管理员");
                }
                newConfig.setListenPort(newListenPort);
                oldConfig.setListenPort(newListenPort);
                //监听新的端口
                portAcceptor.bindPort(newListenPort);
            } else if (!portManager.isAvailable(newRemotePort)) {
                throw new PortConflictException(newRemotePort);
            }
            //释放原来的端口占用
            if (oldListenPort != null) {
                closeCacheByPort(oldListenPort);
            }
        }
    }

    @Override
    public void unregister(ProxyConfig config) throws EtpException {
        closeCacheByPort(config.getListenPort());
        agentManager.getAgentContext(config.getAgentId()).ifPresent(agentContext -> {
            agentManager.removeProxyContextIndex(config.getProxyId());
        });
    }

    private void closeCacheByPort(int listenPort) {
        logger.debug("释放端口 {} ", listenPort);
        portManager.release(listenPort);
        MetricsCollector.removeCollector(listenPort + "");
        portAcceptor.stopPortListen(listenPort);
        streamManager.closeStreams(listenPort);
    }

    @Override
    public void statusChanged(ProxyConfig proxyConfig, boolean newEnabled) {
        String agentId = proxyConfig.getAgentId();
        Integer listenPort = proxyConfig.getListenPort();
        String name = proxyConfig.getName();
        logger.debug("客户端 {} 代理 {} 配置可用状态变更：{} --> {}",agentId,name, proxyConfig.isEnabled(), newEnabled);

        if (newEnabled && !proxyConfig.isEnabled()) {
            logger.debug("监听客户端 {} 代理 {} 端口 {}",agentId, name, listenPort);
            portAcceptor.bindPort(listenPort);
        } else {
            logger.debug("停止客户端 {} 代理 {} 端口 {}",agentId, name, listenPort);
            portAcceptor.stopPortListen(listenPort);
            streamManager.closeStreams(listenPort);
        }
    }
}
