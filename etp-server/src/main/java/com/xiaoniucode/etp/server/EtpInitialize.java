package com.xiaoniucode.etp.server;

import com.xiaoniucode.etp.core.codec.ProtocolType;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.config.ClientInfo;
import com.xiaoniucode.etp.server.config.Dashboard;
import com.xiaoniucode.etp.server.config.PortRange;
import com.xiaoniucode.etp.server.config.ProxyMapping;
import com.xiaoniucode.etp.server.manager.PortAllocator;
import com.xiaoniucode.etp.server.manager.RuntimeStateManager;
import com.xiaoniucode.etp.server.web.core.orm.JdbcFactory;
import com.xiaoniucode.etp.server.web.dao.*;
import com.xiaoniucode.etp.server.web.digest.DigestUtil;
import com.xiaoniucode.etp.server.web.core.orm.transaction.JdbcTransactionTemplate;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;

/**
 * 服务初始化
 *
 * @author liuxin
 */
public final class EtpInitialize {
    private static final Logger logger = LoggerFactory.getLogger(EtpInitialize.class);
    private final static RuntimeStateManager runtimeState = RuntimeStateManager.get();
    private final static AppConfig config = AppConfig.get();
    private static JdbcTransactionTemplate TX;

    /**
     * 需要先初始化SQLite配置，如果存在和toml中相同的配置，则不同步到SQLite，否则进行同步
     */
    public static void initDataConfig() {
        //只有管理面板启动才初始化动态数据
        if (config.getDashboard().getEnable()) {
            TX = new JdbcTransactionTemplate();
            //如果数据库和表不存在则创建
            initDBTable();
            //注册所有客户端配置
            registerDBClientConfig();
            //注册所有端口映射配置
            registerDBProxyConfig();
            //将Toml中的用户信息同步到数据库
            syncDashboardUser();
            //同步系统设置
            syncSystemSettings();
            //每次启动或者重启都删除所有自动注册的映射，避免客户端断线重练导致僵尸映射
            DaoFactory.INSTANCE.getProxyDao().deleteAllAutoRegisterProxy();
        }
        //注册和同步Toml静态配
        registerTomlConfig();
        logger.debug("数据初始化完毕");
    }

    private static void syncSystemSettings() {
        TX.execute(() -> {
            Boolean enableDashboard = config.getDashboard().getEnable();
            if (enableDashboard) {
                // 同步端口范围设置
                JSONObject portRange = DaoFactory.INSTANCE.getSettingDao().getByKey("port_range");
                if (portRange == null) {
                    JSONObject save = new JSONObject();
                    save.put("key", "port_range");
                    save.put("value", config.getPortRange().getStart() + ":" + config.getPortRange().getEnd());
                    DaoFactory.INSTANCE.getSettingDao().insert(save);
                    logger.info("同步端口范围配置到数据库");
                } else {
                    //如果数据库存在配置，采用数据库配置作为系统全局配置
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

    private static void syncDashboardUser() {
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

    private static void registerTomlConfig() {
        Boolean enableDashboard = config.getDashboard().getEnable();
        List<ClientInfo> clients = AppConfig.get().getClients();
        clients.forEach(clientInfo -> {
            String secretKey = clientInfo.getSecretKey();
            String name = clientInfo.getName();
            Integer clientId = null;
            if (!runtimeState.hasClient(secretKey)) {
                //如果开启了管理面板，需要将客户端配置同步到数据库
                if (enableDashboard) {
                    JSONObject clientName = DaoFactory.INSTANCE.getClientDao().getByName(name);
                    if (clientName == null) {
                        JSONObject save = new JSONObject();
                        save.put("secretKey", secretKey);
                        save.put("name", name);
                        clientId = DaoFactory.INSTANCE.getClientDao().insert(save);
                        logger.info("同步静态配置客户端「{}」到数据库", name);
                    } else {
                        logger.warn("无法保存到数据库，已存在同名客户端：{}", clientName);
                    }
                }
                if (clientId == null) {
                    clientId = GlobalIdGenerator.nextId();
                }
                //注册客户端
                clientInfo.setClientId(clientId);
                runtimeState.registerClient(clientInfo);
            } else {
                logger.warn("客户端「{}」 注册失败，已经在数据库被注册", name);
            }
            List<ProxyMapping> proxies = clientInfo.getProxies();
            for (ProxyMapping proxy : proxies) {
                Integer remotePort = proxy.getRemotePort();
                if (!runtimeState.hasProxy(secretKey, remotePort)) {
                    Integer proxyId = null;
                    String type = proxy.getType().name().toLowerCase(Locale.ROOT);
                    if (ProtocolType.TCP.name().equalsIgnoreCase(type) && proxy.getRemotePort() == null) {
                        int allocatePort = PortAllocator.get().allocateAvailablePort();
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

    private static void registerDBClientConfig() {
        JSONArray clients = DaoFactory.INSTANCE.getClientDao().list();
        if (clients != null) {
            for (int i = 0; i < clients.length(); i++) {
                JSONObject client = clients.getJSONObject(i);
                int clientId = client.getInt("id");
                String name = client.getString("name");
                String secretKey = client.getString("secretKey");
                ClientInfo clientInfo = new ClientInfo(clientId, name, secretKey);
                runtimeState.registerClient(clientInfo);
                logger.info("Client {} 已注册", name);
            }
        }
    }

    private static void registerDBProxyConfig() {
        JSONArray proxies = DaoFactory.INSTANCE.getProxyDao().listAllProxies(ProtocolType.TCP.name());
        if (proxies != null) {
            for (int i = 0; i < proxies.length(); i++) {
                JSONObject proxy = proxies.getJSONObject(i);
                String secretKey = proxy.getString("secretKey");
                ProxyMapping proxyMapping = new ProxyMapping(
                        ProtocolType.getType(proxy.getString("type").toLowerCase(Locale.ROOT)),
                        proxy.getInt("localPort"),
                        proxy.getInt("remotePort"));
                proxyMapping.setProxyId(proxy.getInt("id"));
                proxyMapping.setName(proxy.getString("name"));
                proxyMapping.setStatus(proxy.getInt("status"));
                runtimeState.registerProxy(secretKey, proxyMapping);
            }
        }
    }

    /**
     * 只有不存在的时候才会创建表
     */
    private static void initDBTable() {
        logger.debug("开始初始化数据库表");
        createAuthTokenTable();
        createUserTable();
        createClientTable();
        createProxiesTable();
        createSystemSettingsTable();
        logger.debug("数据库表初始化完毕");
    }

    private static void createSystemSettingsTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS settings (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    key TEXT NOT NULL UNIQUE,       -- 设置键
                    value TEXT NOT NULL             -- 设置值
                );
                CREATE INDEX IF NOT EXISTS idx_settings_key ON settings(key);
                """;
        JdbcFactory.getJdbc().execute(sql);
    }

    private static void createAuthTokenTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS auth_tokens (
                    token TEXT PRIMARY KEY,
                    uid INTEGER NOT NULL,
                    username TEXT NOT NULL,
                    expiredAt INTEGER NOT NULL,
                    createdAt INTEGER DEFAULT (datetime('now')),
                    FOREIGN KEY (uid) REFERENCES users(id)
                );
                CREATE INDEX IF NOT EXISTS idx_auth_tokens_expiredAt ON auth_tokens(expiredAt);
                """;
        JdbcFactory.getJdbc().execute(sql);
    }

    private static void createUserTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL
                );
                """;
        JdbcFactory.getJdbc().execute(sql);
    }

    private static void createProxiesTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS proxies (
                    id               INTEGER PRIMARY KEY AUTOINCREMENT,  -- 自增主键
                    clientId         INTEGER NOT NULL,                   -- 所属客户端ID
                    name             TEXT NOT NULL,                      -- 代理名称
                    type             TEXT NOT NULL,                      -- 协议类型（如 "TCP"、"HTTP"）
                    autoRegistered   INTEGER NOT NULL DEFAULT 0,         -- 注册类型（1：自动注册、0手动注册）
                    localPort        INTEGER NOT NULL,                   -- 内网端口（如 3306）
                    remotePort       INTEGER,                            -- 远程服务端口（对外暴露的端口）
                    domains          TEXT,                               -- http协议域名，多个域名用逗号分隔
                    status           INTEGER NOT NULL DEFAULT 1,         -- 状态：1=开启，0=关闭
                    createdAt        TEXT DEFAULT (datetime('now')),     -- 创建时间
                    updatedAt        TEXT DEFAULT (datetime('now'))      -- 更新时间
                );
                """;
        JdbcFactory.getJdbc().execute(sql);
    }

    private static void createClientTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS clients (
                    id         INTEGER PRIMARY KEY AUTOINCREMENT,  -- 自增ID
                    name       TEXT    NOT NULL UNIQUE,            -- 客户端名称
                    secretKey  TEXT    NOT NULL UNIQUE,            -- 密钥
                    createdAt TEXT    DEFAULT (datetime('now')),   -- 创建时间
                    updatedAt TEXT    DEFAULT (datetime('now'))    -- 更新时间
                );
                 CREATE INDEX IF NOT EXISTS idx_clients_name_secretkey ON clients (name, secretKey);
                """;
        JdbcFactory.getJdbc().execute(sql);
    }
}
