package com.xiaoniucode.etp.server.proxy;

import com.xiaoniucode.etp.common.utils.StringUtils;
import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.server.exceptions.EtpException;
import com.xiaoniucode.etp.server.exceptions.PortConflictException;
import com.xiaoniucode.etp.server.port.PortManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class TcpOperationDelegate implements ProxyOperationDelegate {
    @Autowired
    private PortManager portManager;

    @Override
    public boolean supports(ProxyConfig config) {
        return config.isTcp();
    }

    @Override
    public void validate(ProxyConfig config) throws EtpException {
        Integer port = config.getRemotePort();
        if (port != null && !portManager.isAvailable(port)) {
            throw new PortConflictException(port);
        }
        if (StringUtils.hasText(config.getName())) {
            throw new EtpException("代理名不能为空！");
        }
    }

    @Override
    public void onCreate(ProxyConfig config) throws EtpException {
        Integer remotePort = config.getRemotePort();
        if (remotePort == null) {
            remotePort = portManager.acquire();
        }
        if (remotePort == null) {
            throw new EtpException("无可用端口，请联系管理员");
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
