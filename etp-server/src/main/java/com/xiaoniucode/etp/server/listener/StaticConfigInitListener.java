package com.xiaoniucode.etp.server.listener;

import com.xiaoniucode.etp.common.utils.StringUtils;
import com.xiaoniucode.etp.core.codec.ProtocolType;
import com.xiaoniucode.etp.core.event.EventListener;
import com.xiaoniucode.etp.server.generator.GlobalIdGenerator;
import com.xiaoniucode.etp.server.config.domain.*;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.config.ConfigHelper;
import com.xiaoniucode.etp.server.event.DatabaseInitEvent;
import com.xiaoniucode.etp.server.manager.PortPool;
import com.xiaoniucode.etp.server.manager.RuntimeStateManager;
import com.xiaoniucode.etp.server.web.core.orm.transaction.JdbcTransactionTemplate;
import com.xiaoniucode.etp.server.web.dao.DaoFactory;
import com.xiaoniucode.etp.server.web.common.DigestUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class StaticConfigInitListener implements EventListener<DatabaseInitEvent> {
    private static final Logger logger = LoggerFactory.getLogger(StaticConfigInitListener.class);
    private final static RuntimeStateManager runtimeState = RuntimeStateManager.get();
    private final static AppConfig config = ConfigHelper.get();
    private static JdbcTransactionTemplate TX;

    /**
     * 需要先初始化SQLite配置，如果存在和toml中相同的配置，则不同步到SQLite，否则进行同步
     */
    @Override
    public void onEvent(DatabaseInitEvent event) {
        //只有管理面板启动才初始化动态数据
        if (config.getDashboard().getEnable()) {
            TX = new JdbcTransactionTemplate();
            //注册所有客户端配置
            registerDBClientConfig();
            //注册所有端口映射配置
            registerDbProxyConfig();
            //将Toml中的用户信息同步到数据库
            syncDashboardUser();
            //同步系统设置
            syncSystemSettings();
            //每次启动或者重启都删除所有自动注册的映射，避免客户端断线重练导致僵尸代理
            DaoFactory.INSTANCE.getProxyDao().deleteAllAutoRegisterProxy();
        }
        //注册和同步Toml静态配
        registerTomlConfig();
        logger.debug("数据初始化完毕");
    }

    private void syncSystemSettings() {
        TX.execute(() -> {
            if (config.getDashboard().getEnable()) {
                // 同步端口范围设置
                JSONObject portRange = DaoFactory.INSTANCE.getSettingDao().getByKey("port_range");
                if (portRange == null) {
                    JSONObject save = new JSONObject();
                    save.put("key", "port_range");
                    save.put("value", config.getPortRange().getStart() + ":" + config.getPortRange().getEnd());
                    DaoFactory.INSTANCE.getSettingDao().insert(save);
                    logger.info("同步端口范围配置到数据库");
                } else {
                    //如果数据库存在配置，采用数据库配置作为全局配置
                    String range = portRange.getString("value");
                    String[] split = range.split(":");
                    PortRange configRange = config.getPortRange();
                    configRange.setStart(Integer.parseInt(split[0]));
                    configRange.setEnd(Integer.parseInt(split[1]));
                }
            }
            return null;
        });
    }

    private void syncDashboardUser() {
        TX.execute(() -> {
            Dashboard dashboard = config.getDashboard();
            Boolean reset = dashboard.getReset();
            String username = dashboard.getUsername();
            String password = dashboard.getPassword();
            JSONObject save = new JSONObject();
            save.put("username", username);
            save.put("password", DigestUtil.encode(password, username));
            JSONObject user = DaoFactory.INSTANCE.getUserDao().getByUsername(username);
            //没有直接添加用户
            if (user == null) {
                DaoFactory.INSTANCE.getUserDao().insert(save);
                logger.info("注册用户 {}", username);
            }
            //如果数据库已经存在用户了，如果reset=true则重置
            if (reset) {
                //删除所有用户
                DaoFactory.INSTANCE.getUserDao().deleteAll();
                //重新注册
                DaoFactory.INSTANCE.getUserDao().insert(save);
                logger.info("重置面板用户登录信息 {}", username);
            }
            return null;
        });
    }

    private void registerTomlConfig() {
        Boolean enableDashboard = config.getDashboard().getEnable();
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

    private void registerDBClientConfig() {
        JSONArray clients = DaoFactory.INSTANCE.getClientDao().list();
        if (clients != null) {
            for (int i = 0; i < clients.length(); i++) {
                JSONObject client = clients.getJSONObject(i);
                int clientId = client.getInt("id");
                String name = client.getString("name");
                String secretKey = client.getString("secretKey");
                ClientInfo clientInfo = new ClientInfo(name,secretKey,clientId);
                runtimeState.registerClient(clientInfo);
            }
        }
    }

    private void registerDbProxyConfig() {
        JSONArray proxies = DaoFactory.INSTANCE.getProxyDao().listAllProxies(null);
        if (proxies != null) {
            for (int i = 0; i < proxies.length(); i++) {
                JSONObject proxy = proxies.getJSONObject(i);
                String secretKey = proxy.getString("secretKey");
                ProtocolType type = ProtocolType.getType(proxy.getString("type").toLowerCase(Locale.ROOT));

                ProxyMapping proxyMapping = new ProxyMapping();
                proxyMapping.setType(type);
                proxyMapping.setLocalPort(proxy.getInt("localPort"));
                if (ProtocolType.TCP.equals(type)) {
                    proxyMapping.setRemotePort(proxy.getInt("remotePort"));
                } else if (ProtocolType.HTTP.equals(type)) {
                    String d = proxy.getString("domains");
                    if (StringUtils.hasText(d)) {
                        String[] split = d.split(",");
                        proxyMapping.setDomains(new HashSet<>(List.of(split)));
                    }
                }
                proxyMapping.setProxyId(proxy.getInt("id"));
                proxyMapping.setName(proxy.getString("name"));
                proxyMapping.setStatus(proxy.getInt("status"));
                runtimeState.registerProxy(secretKey, proxyMapping);
            }
        }
    }
}
