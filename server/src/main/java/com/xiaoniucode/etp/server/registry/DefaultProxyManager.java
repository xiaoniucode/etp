package com.xiaoniucode.etp.server.registry;

import com.xiaoniucode.etp.common.utils.StringUtils;
import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.server.exceptions.EtpException;
import com.xiaoniucode.etp.server.generator.UUIDGenerator;
import com.xiaoniucode.etp.server.store.ProxyStore;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class  DefaultProxyManager implements ProxyManager {
    private final UUIDGenerator uuidGenerator;
    private final List<ProxyConfigListener> listeners = new CopyOnWriteArrayList<>();
    private final ProxyStore proxyStore;
    private final ProxyOperationDelegateFactory proxyRegisterDelegateFactory;

    public DefaultProxyManager(List<ProxyConfigListener> allCallbacks,
                               ProxyStore proxyStore,
                               UUIDGenerator uuidGenerator,
                               ProxyOperationDelegateFactory proxyRegisterDelegateFactory) {
        this.uuidGenerator = uuidGenerator;
        this.proxyStore = proxyStore;
        this.proxyRegisterDelegateFactory = proxyRegisterDelegateFactory;
        if (allCallbacks != null) {
            this.listeners.addAll(allCallbacks);
        }
    }
    @Override
    public synchronized ProxyConfig register(ProxyConfig config) throws EtpException {
        String clientId = config.getClientId();
        if (!StringUtils.hasText(clientId)) {
            throw new EtpException("clientId 不能为空");
        }
        //检查是否已经存在了，如果存在则更新、不存在则创建
        Optional<ProxyConfig> existing = findByClientIdAndName(clientId, config.getName());
        ProxyOperationDelegate delegate = proxyRegisterDelegateFactory.getDelegate(config);
        if (existing.isPresent()) {
            ProxyConfig oldConfig = existing.get();
            if (ProxyConfigComparator.isChanged(oldConfig, config)) {
                //如果配置发生了变化
                return updateProxy(clientId, oldConfig, config, delegate);
            } else {
                return oldConfig;
            }
        } else {
            //新建
            return createProxy(clientId, config, delegate);
        }
    }

    private ProxyConfig updateProxy(String clientId, ProxyConfig oldConfig, ProxyConfig newConfig, ProxyOperationDelegate delegate) throws EtpException {
        //参数校验
        delegate.validate(newConfig);

        //执行更新操作
        newConfig.setProxyId(oldConfig.getProxyId());
        newConfig.setClientId(clientId);

        delegate.onUpdate(oldConfig,newConfig);

        //触发监听器通知
        listeners.forEach(listener -> listener.onUpdated(oldConfig, newConfig));
        return oldConfig;
    }

    private ProxyConfig createProxy(String clientId, ProxyConfig newConfig, ProxyOperationDelegate delegate) throws EtpException {
        String proxyId = uuidGenerator.uuid32();

        //参数校验
        delegate.validate(newConfig);
        //执行创建
        newConfig.setProxyId(proxyId);
        newConfig.setClientId(clientId);
        delegate.onCreate(newConfig);

        //添加到存储
        proxyStore.add(newConfig);

        //通知
        listeners.forEach(listener -> listener.onAdded(newConfig));
        return newConfig;
    }


    @Override
    public synchronized Optional<ProxyConfig> delete(String proxyId) {
        Optional<ProxyConfig> opt = findById(proxyId);
        if (opt.isPresent()) {
            ProxyConfig proxyConfig = opt.get();
            ProxyOperationDelegate delegate = proxyRegisterDelegateFactory.getDelegate(proxyConfig);
            //删除操作
            delegate.onDelete(proxyConfig);
            //删除存储
            proxyStore.deleteById(proxyId);
            //通知
            listeners.forEach(listener -> listener.onDeleted(proxyConfig));
            return Optional.of(proxyConfig);
        }
        return Optional.empty();

    }

    @Override
    public Optional<ProxyConfig> findByRemotePort(Integer port) {
        return Optional.ofNullable(proxyStore.findByRemotePort(port));
    }

    @Override
    public List<ProxyConfig> findAll() {
        return proxyStore.findAll();
    }

    @Override
    public List<ProxyConfig> findAllTcpProxies() {
        return proxyStore.findAllTcpProxies();
    }

    @Override
    public List<ProxyConfig> findAllHttpProxies() {
        return proxyStore.findAllHttpProxies();
    }

    @Override
    public List<ProxyConfig> findByClientId(String clientId) {
        return proxyStore.findByClientId(clientId);
    }

    @Override
    public Optional<ProxyConfig> findById(String proxyId) {
        return Optional.ofNullable(proxyStore.findById(proxyId));
    }

    @Override
    public Optional<ProxyConfig> findByDomain(String domain) {
        return Optional.ofNullable(proxyStore.findByDomain(domain));
    }

    @Override
    public Optional<ProxyConfig> findByClientIdAndName(String clientId, String proxyName) {
        return Optional.ofNullable(proxyStore.findByClientIdAndName(clientId, proxyName));
    }

    @Override
    public synchronized ProxyConfig changeStatus(String proxyId, boolean enable) {
        Optional<ProxyConfig> opt = findById(proxyId);
        if (opt.isPresent()) {
            ProxyConfig proxyConfig = opt.get();
            if (proxyConfig.isEnable() != enable) {
                proxyConfig.setEnable(enable);
                listeners.forEach(listener -> listener.onStatusChanged(proxyConfig, enable));
            }
            return proxyConfig;
        }

        return null;
    }
}
