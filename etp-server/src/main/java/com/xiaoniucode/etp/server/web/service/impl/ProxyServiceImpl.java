package com.xiaoniucode.etp.server.web.service.impl;

import com.xiaoniucode.etp.server.manager.ProxyManager;
import com.xiaoniucode.etp.server.web.domain.Proxy;
import com.xiaoniucode.etp.server.web.repository.ProxyRepository;
import com.xiaoniucode.etp.server.web.repository.ProxyDomainRepository;
import com.xiaoniucode.etp.server.web.service.ProxyService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProxyServiceImpl implements ProxyService {

    @Autowired
    private ProxyRepository proxiesRepository;

    @Autowired
    private ProxyDomainRepository proxyDomainRepository;
    @Autowired
    private ProxyManager proxyManager;
    @Override
    public Proxy getProxy(Proxy req) {
        return proxiesRepository.findById(req.getId()).orElse(null);
    }

    @Override
    public List<Proxy> proxies(String type) {
        return proxiesRepository.findByProtocol(type);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public Proxy addTcpProxy(Proxy req) {
        //添加到管理器
        //启动或者停止服务

        return req;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void updateTcpProxy(Proxy req) {

    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public Proxy addHttpProxy(Proxy req) {

        return req;
    }


    /**
     * 切换端口映射状态
     */
    @Override
    @Transactional(rollbackOn = Exception.class)
    public void switchProxyStatus(Proxy req) {
        //更新管理器
        //停止tcp服务或者开启 状态
        //保存到数据库
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void deleteProxy(Proxy req) {
        //从管理器删除
        //删除session
        //关闭该客户端有关的所有session连接
        //通知agent断开
        //端口服务需要停止

    }

    @Override
    public void deleteProxiesByClient(int clientId) {

    }
}
