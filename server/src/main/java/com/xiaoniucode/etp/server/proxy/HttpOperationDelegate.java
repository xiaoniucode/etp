package com.xiaoniucode.etp.server.proxy;

import com.xiaoniucode.etp.common.utils.StringUtils;
import com.xiaoniucode.etp.core.domain.DomainConfig;
import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.enums.DomainType;
import com.xiaoniucode.etp.server.exceptions.DomainConflictException;
import com.xiaoniucode.etp.server.exceptions.EtpException;
import com.xiaoniucode.etp.server.vhost.DomainGenerator;
import com.xiaoniucode.etp.server.vhost.DomainInfo;
import com.xiaoniucode.etp.server.vhost.DomainManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class HttpOperationDelegate implements ProxyOperationDelegate {
    @Autowired
    private DomainManager domainManager;
    @Autowired
    private DomainGenerator domainGenerator;
    @Override
    public boolean supports(ProxyConfig config) {
        return config.isHttp();
    }

    @Override
    public void validate(ProxyConfig config) throws EtpException {
        DomainConfig domainInfo = config.getDomainInfo();
        DomainType domainType = domainInfo.getDomainType();
        if (DomainType.CUSTOM_DOMAIN == domainType) {
            Set<String> customDomains = domainInfo.getCustomDomains();
            for (String domain : customDomains) {
                if (domainManager.exists(domain)) {
                    throw new DomainConflictException(domain);
                }
            }
        }
        if (StringUtils.hasText(config.getName())) {
            throw new EtpException("代理名不能为空！");
        }
    }

    @Override
    public void onCreate(ProxyConfig config) throws EtpException {

        Set<DomainInfo> domains = domainGenerator.generate(config);

        DomainConfig domainInfo = config.getDomainInfo();


    }

    @Override
    public void onUpdate(ProxyConfig oldConfig, ProxyConfig newConfig) throws EtpException {

    }

    @Override
    public void onDelete(ProxyConfig proxyConfig) throws EtpException {
        //释放域名
        domainManager.clearDomain(proxyConfig.getProxyId());
    }
}
