package com.xiaoniucode.etp.server.listener;

import com.xiaoniucode.etp.core.codec.ProtocolType;
import com.xiaoniucode.etp.core.event.EventListener;
import com.xiaoniucode.etp.server.config.ConfigHelper;
import com.xiaoniucode.etp.server.config.domain.ClientInfo;
import com.xiaoniucode.etp.server.config.domain.ProxyMapping;
import com.xiaoniucode.etp.server.event.TunnelBindEvent;
import com.xiaoniucode.etp.server.generator.GlobalIdGenerator;
import com.xiaoniucode.etp.server.manager.PortPool;
import com.xiaoniucode.etp.server.manager.RuntimeStateManager;
import com.xiaoniucode.etp.server.web.dao.DaoFactory;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;

public class StaticConfigInitListener implements EventListener<TunnelBindEvent> {
    private final static Logger logger = LoggerFactory.getLogger(StaticConfigInitListener.class);
    private final static RuntimeStateManager runtimeState = RuntimeStateManager.get();
    @Override
    public void onEvent(TunnelBindEvent event) {
        registerTomlConfig();
    }
    private void registerTomlConfig() {
        Boolean enableDashboard =  ConfigHelper.get().getDashboard().getEnable();
        List<ClientInfo> clients = ConfigHelper.get().getClients();
        clients.forEach(clientInfo -> {
            ClientInfo client = clientInfo;
            String secretKey = client.getSecretKey();
            String name = client.getName();
            Integer clientId = null;
            if (!runtimeState.hasClient(secretKey)) {
                //如果开启了管理面板，需要将客户端配置同步到数据库
                if (enableDashboard) {
                    JSONObject existClient = DaoFactory.INSTANCE.getClientDao().getByName(name);
                    if (existClient == null) {
                        JSONObject save = new JSONObject();
                        save.put("secretKey", secretKey);
                        save.put("name", name);
                        clientId = DaoFactory.INSTANCE.getClientDao().insert(save);
                        logger.info("同步静态配置客户端「{}」到数据库", name);
                    } else {
                        logger.warn("无法保存客户端到数据库，已存在同名客户端：{}", existClient.getString("name"));
                    }
                }
                if (clientId == null) {
                    clientId = GlobalIdGenerator.nextId();
                }
                //注册客户端
                client.setClientId(clientId);
                runtimeState.registerClient(client);
            } else {
                clientId = runtimeState.getClient(secretKey).getClientId();
                logger.warn("客户端「{}」 注册失败，已经在数据库被注册", name);
            }

            List<ProxyMapping> proxies = client.getProxies();
            for (ProxyMapping proxy : proxies) {
                Integer remotePort = proxy.getRemotePort();
                if (!runtimeState.hasProxy(secretKey, remotePort)) {
                    Integer proxyId = null;
                    String type = proxy.getType().name().toLowerCase(Locale.ROOT);
                    if (ProtocolType.TCP.name().equalsIgnoreCase(type) && proxy.getRemotePort() == null) {
                        int allocatePort = PortPool.get().allocateAvailablePort();
                        proxy.setRemotePort(allocatePort);
                    }
                    //如果开启了管理面板，需要将映射配置同步到数据库
                    if (enableDashboard) {
                        if (DaoFactory.INSTANCE.getProxyDao().getProxy(clientId, proxy.getName()) == null) {
                            JSONObject save = new JSONObject();
                            save.put("clientId", clientId);
                            save.put("name", proxy.getName());
                            save.put("localPort", proxy.getLocalPort());
                            save.put("status", proxy.getStatus());
                            save.put("type", type);
                            save.put("autoRegistered", 0);
                            if (ProtocolType.HTTP.name().equalsIgnoreCase(type) && !proxy.getDomains().isEmpty()) {
                                save.put("domains", String.join(",", proxy.getDomains()));
                            }
                            if (type.equalsIgnoreCase(ProtocolType.TCP.name())) {
                                save.put("remotePort", proxy.getRemotePort());
                                proxyId = DaoFactory.INSTANCE.getProxyDao().insert(save);
                                logger.info("客户端 {}-映射名 {}-公网端口 {} 已同步到数据库", clientId, proxy.getName(), proxy.getRemotePort());
                            }
                            if (type.equalsIgnoreCase(ProtocolType.HTTP.name())) {
                                proxyId = DaoFactory.INSTANCE.getProxyDao().insert(save);
                            }
                        } else {
                            logger.warn("无法保存代理到数据库，存在同名隧道名：{}", proxy.getName());
                        }
                    }
                    //生成一个临时业务ID
                    if (proxyId == null) {
                        proxyId = GlobalIdGenerator.nextId();
                    }
                    proxy.setProxyId(proxyId);
                    runtimeState.registerProxy(secretKey, proxy);
                } else {
                    logger.warn("同步取消，该客户端公网端口「{}-{}」已经被注册", name, remotePort);
                }
            }
        });
    }
}
