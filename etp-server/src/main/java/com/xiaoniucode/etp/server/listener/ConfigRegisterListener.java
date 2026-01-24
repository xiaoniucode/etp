package com.xiaoniucode.etp.server.listener;

import com.xiaoniucode.etp.core.codec.ProtocolType;
import com.xiaoniucode.etp.core.event.EventListener;
import com.xiaoniucode.etp.server.config.domain.*;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.config.ConfigHelper;
import com.xiaoniucode.etp.server.event.DatabaseInitEvent;
import com.xiaoniucode.etp.server.manager.ClientManager;
import com.xiaoniucode.etp.server.manager.ProxyManager;
import com.xiaoniucode.etp.server.web.core.orm.transaction.JdbcTransactionTemplate;
import com.xiaoniucode.etp.server.web.dao.DaoFactory;
import com.xiaoniucode.etp.server.web.common.DigestUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class ConfigRegisterListener implements EventListener<DatabaseInitEvent> {
    private static final Logger logger = LoggerFactory.getLogger(ConfigRegisterListener.class);
    private static JdbcTransactionTemplate TX;

    @Override
    public void onEvent(DatabaseInitEvent event) {
        AppConfig config = ConfigHelper.get();
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
            logger.debug("数据初始化完毕");
        }
    }

    private void syncSystemSettings() {
        TX.execute(() -> {
            if (ConfigHelper.get().getDashboard().getEnable()) {
                // 同步端口范围设置
                JSONObject portRange = DaoFactory.INSTANCE.getSettingDao().getByKey("port_range");
                if (portRange == null) {
                    JSONObject save = new JSONObject();
                    save.put("key", "port_range");
                    save.put("value", ConfigHelper.get().getPortRange().getStart() + ":" + ConfigHelper.get().getPortRange().getEnd());
                    DaoFactory.INSTANCE.getSettingDao().insert(save);
                    logger.info("同步端口范围配置到数据库");
                } else {
                    //如果数据库存在配置，采用数据库配置作为全局配置
                    String range = portRange.getString("value");
                    String[] split = range.split(":");
                    PortRange configRange = ConfigHelper.get().getPortRange();
                    configRange.setStart(Integer.parseInt(split[0]));
                    configRange.setEnd(Integer.parseInt(split[1]));
                }
            }
            return null;
        });
    }

    private void syncDashboardUser() {
        TX.execute(() -> {
            Dashboard dashboard = ConfigHelper.get().getDashboard();
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

    private void registerDBClientConfig() {
        JSONArray clients = DaoFactory.INSTANCE.getClientDao().list();
        if (clients != null) {
            for (int i = 0; i < clients.length(); i++) {
                //todo 需要检查配置中是否存在了
                JSONObject client = clients.getJSONObject(i);
                int clientId = client.getInt("id");
                String name = client.getString("name");
                String secretKey = client.getString("secretKey");
                ClientInfo clientInfo = new ClientInfo(name, secretKey, clientId);
                ClientManager.addClient(clientInfo);
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

                ProxyConfig proxyConfig = new ProxyConfig();
                proxyConfig.setType(type);
                proxyConfig.setLocalIP(proxy.getString("localIP"));
                proxyConfig.setLocalPort(proxy.getInt("localPort"));
                if (ProtocolType.TCP.equals(type)) {
                    proxyConfig.setRemotePort(proxy.getInt("remotePort"));
                } else if (ProtocolType.HTTP.equals(type)||ProtocolType.HTTPS.equals(type)) {
                    JSONArray d = proxy.getJSONArray("domains");
                    Set<String> domains = new HashSet<>();
                    for (Object item : d) {
                        JSONObject itemJson = (JSONObject) item;
                        domains.add(itemJson.getString("domain"));
                    }
                    proxyConfig.setDomains(domains);
                }
                proxyConfig.setProxyId(proxy.getInt("id"));
                proxyConfig.setName(proxy.getString("name"));
                proxyConfig.setStatus(proxy.getInt("status"));
                ProxyManager.addProxy(secretKey, proxyConfig);
            }
        }
    }
}
