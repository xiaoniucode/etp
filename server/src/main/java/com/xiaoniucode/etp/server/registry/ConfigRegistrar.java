package com.xiaoniucode.etp.server.registry;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.server.exceptions.EtpException;
import org.javers.core.diff.Diff;

public interface ConfigRegistrar {
    boolean supports(ProxyConfig config);

    void validate(ProxyConfig config) throws EtpException;

    void register(ProxyConfig config) throws EtpException;

    void reregister(ProxyConfig oldConfig, ProxyConfig newConfig, Diff diff) throws EtpException;

    void unregister(ProxyConfig proxyConfig) throws EtpException;

    default void statusChanged(ProxyConfig proxyConfig, boolean newEnabled) {}
}