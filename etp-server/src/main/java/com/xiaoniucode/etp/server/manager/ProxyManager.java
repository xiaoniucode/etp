package com.xiaoniucode.etp.server.manager;

import com.xiaoniucode.etp.core.LanInfo;
import com.xiaoniucode.etp.core.codec.ProtocolType;
import com.xiaoniucode.etp.server.config.domain.ProxyConfig;
import com.xiaoniucode.etp.server.manager.domain.ProxyConfigExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

@Component
public class ProxyManager {
    private static final Logger logger = LoggerFactory.getLogger(ProxyManager.class);

    public boolean hasDomain(String domain) {
        return false;
    }

    public LanInfo getLanInfoByRemotePort(Integer port) {
        return new LanInfo("localhost", 3306);
    }

    public LanInfo getLanInfoByDomain(String domain) {
        return new LanInfo("localhost", 3306);
    }


    public ProxyConfigExt addProxy(ProxyConfig proxyConfig, Consumer<ProxyConfigExt> proxyConfigExt) {
        return null;
    }

    public List<ProxyConfigExt> getProxyConfigsBySessionId(String sessionId) {
        ProxyConfigExt proxyConfigExt = new ProxyConfigExt();
        proxyConfigExt.setProtocol(ProtocolType.TCP);
        proxyConfigExt.setRemotePort(3307);
        proxyConfigExt.setLocalIp("localhost");
        proxyConfigExt.setLocalPort(3306);
        return List.of(proxyConfigExt);
    }
}
