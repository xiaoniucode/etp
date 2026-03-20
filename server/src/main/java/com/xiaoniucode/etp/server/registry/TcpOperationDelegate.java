package com.xiaoniucode.etp.server.registry;

import com.xiaoniucode.etp.common.utils.StringUtils;
import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.enums.ClientType;
import com.xiaoniucode.etp.server.exceptions.EtpException;
import com.xiaoniucode.etp.server.exceptions.PortConflictException;
import com.xiaoniucode.etp.server.port.PortManager;
import com.xiaoniucode.etp.server.statemachine.agent.AgentManager;
import com.xiaoniucode.etp.server.store.ProxyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class TcpOperationDelegate implements ProxyOperationDelegate {
    private final Logger logger = LoggerFactory.getLogger(TcpOperationDelegate.class);
    @Autowired
    private PortManager portManager;
    @Autowired
    private AgentManager agentManager;
    @Autowired
    private ProxyStore proxyStore;

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
    public void onCreate(ProxyConfig config) throws EtpException {
        Integer remotePort = config.getRemotePort();
        if (remotePort != null && !portManager.isAvailable(remotePort)) {
            logger.error("端口已被占用: {}", remotePort);
            throw new PortConflictException(remotePort);
        }

        ClientType clientType = config.getClientType();
        String name = config.getName();
        ProxyConfig exist = proxyStore.findByClientIdAndName(config.getClientId(), name);
        //处理临时客户端名称冲突
        if (exist != null && clientType == ClientType.BINARY_DEVICE) {
            throw new EtpException("代理配置名称不能重复");
        }
        if (remotePort == null) {
            remotePort = portManager.acquire();
        }
        if (remotePort == null) {
            throw new EtpException("无可用端口，请联系管理员");
        }
        //处理临时客户端名字重复
        if (exist != null && (clientType == ClientType.SESSION_CLINT)) {
            config.setName(remotePort + "." + name);
        }
        config.setRemotePort(remotePort);
    }

    @Override
    public void onUpdate(ProxyConfig oldConfig, ProxyConfig newConfig) throws EtpException {
        Integer oldRemotePort = oldConfig.getRemotePort();
        Integer newRemotePort = newConfig.getRemotePort();
        if (!Objects.equals(oldRemotePort, newRemotePort)) {
            if (newRemotePort == null) {
                newRemotePort = portManager.acquire();
                if (newRemotePort == null) {
                    throw new EtpException("无可用端口，请联系管理员");
                }
                newConfig.setRemotePort(newRemotePort);
                oldConfig.setRemotePort(newRemotePort);
            } else if (!portManager.isAvailable(newRemotePort)) {
                throw new PortConflictException(newRemotePort);
            }
            //释放原来的端口占用
            if (oldRemotePort != null) {
                portManager.release(oldRemotePort);
            }
        }
        //更新其他信息

    }

    @Override
    public void onDelete(ProxyConfig proxyConfig) throws EtpException {
        portManager.release(proxyConfig.getRemotePort());
    }
}
