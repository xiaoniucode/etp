package com.xiaoniucode.etp.server.web.service.impl;

import com.xiaoniucode.etp.server.web.repository.ProxyRepository;
import com.xiaoniucode.etp.server.web.repository.ProxyDomainRepository;
import com.xiaoniucode.etp.server.web.service.ProxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProxyServiceImpl implements ProxyService {

    @Autowired
    private ProxyRepository proxiesRepository;

    @Autowired
    private ProxyDomainRepository proxyDomainRepository;

}
