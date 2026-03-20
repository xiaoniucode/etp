package com.xiaoniucode.etp.server.registry;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.server.exceptions.EtpException;

/**
 * 处理代理配置的创建和更新、删除
 */
public interface ProxyOperationDelegate {
    boolean supports(ProxyConfig config);

    void validate(ProxyConfig config) throws EtpException;

    void onCreate(ProxyConfig config) throws EtpException;

    void onUpdate(ProxyConfig oldConfig, ProxyConfig newConfig) throws EtpException;

    void onDelete(ProxyConfig proxyConfig) throws EtpException;
}