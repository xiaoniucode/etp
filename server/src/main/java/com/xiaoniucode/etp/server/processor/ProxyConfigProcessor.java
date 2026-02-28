package com.xiaoniucode.etp.server.processor;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.enums.ProtocolType;

public interface ProxyConfigProcessor {
    boolean supports(ProtocolType protocol);
    void process(ProxyConfig proxyConfig);
}