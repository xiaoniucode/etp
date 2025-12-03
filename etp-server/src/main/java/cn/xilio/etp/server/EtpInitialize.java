package cn.xilio.etp.server;

import cn.xilio.etp.core.protocol.ProtocolType;
import cn.xilio.etp.server.config.AppConfig;
import cn.xilio.etp.server.config.ClientInfo;
import cn.xilio.etp.server.config.Dashboard;
import cn.xilio.etp.server.config.ProxyMapping;
import cn.xilio.etp.server.manager.RuntimeState;
import cn.xilio.etp.server.web.ConfigService;
import cn.xilio.etp.server.web.ConfigStore;
import cn.xilio.etp.server.web.SQLiteUtils;
import cn.xilio.etp.server.web.digest.DigestUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;

/**
 * @author liuxin
 */
public final class EtpInitialize {
    private static Logger logger = LoggerFactory.getLogger(EtpInitialize.class);
    private final static ConfigStore configStore = new ConfigStore();
    private final static RuntimeState runtimeState = RuntimeState.get();
    private final static AppConfig config = AppConfig.get();

    /**
     * 需要先初始化SQLite配置，如果存在和toml中相同的配置，则不同步到SQLite，否则进行同步
     */
    public static void initDataConfig() {
        //只有管理面板启动才初始化动态数据
        if (config.getDashboard().getEnable()) {
            //如果数据库和表不存在则创建
            initDBTable();
            //注册所有客户端配置
            registerDBClientConfig();
            //注册所有端口映射配置
            registerDBProxyConfig();
            //将Toml中的用户信息同步到数据库
            syncDashboardUser();
        }
        //注册和同步Toml静态配
        registerTomlConfig();
    }

    private static void syncDashboardUser() {
        Dashboard dashboard = config.getDashboard();
        Boolean reset = dashboard.getReset();
        String username = dashboard.getUsername();
        String password = dashboard.getPassword();
        JSONObject save = new JSONObject();
        save.put("username", username);
        save.put("password", DigestUtil.encode(password, username));
        JSONObject user = ConfigService.getUserByUsername(username);
        //没有直接添加用户
        if (user == null) {
            ConfigService.registerUser(save);
            logger.info("注册用户 {}", username);
            return;
        }
        //如果数据库已经存在用户了，如果reset=true则重置
        if (reset) {
            //删除所有用户
            ConfigService.deleteAll();
            //重新注册
            ConfigService.registerUser(save);
            logger.info("重置面板用户登录信息 {}", username);
        }
    }

    private static void registerTomlConfig() {
        Boolean enableDashboard = config.getDashboard().getEnable();
        List<ClientInfo> clients = AppConfig.get().getClients();
        clients.forEach(clientInfo -> {
            String secretKey = clientInfo.getSecretKey();
            String name = clientInfo.getName();
            if (!runtimeState.hasClient(secretKey)) {
                //注册客户端
                runtimeState.registerClient(clientInfo);
                //如果开启了管理面板，需要将客户端配置同步到数据库
                if (enableDashboard) {
                    JSONObject save = new JSONObject();
                    save.put("secretKey", secretKey);
                    save.put("name", name);
                    configStore.addClient(save);
                    logger.info("成功将客户端配置「{}」同步到数据库", name);
                }
                List<ProxyMapping> proxies = clientInfo.getProxies();
                proxies.forEach(proxy -> {
                    Integer remotePort = proxy.getRemotePort();
                    if (!runtimeState.hasProxy(secretKey, remotePort)) {
                        //注册端口映射
                        runtimeState.registerProxy(secretKey, proxy);
                        //如果开启了管理面板，需要将映射配置同步到数据库
                        if (enableDashboard) {
                            JSONObject existClient = configStore.getClientBySecretKey(secretKey);
                            JSONObject save = new JSONObject();
                            save.put("clientId", existClient.getInt("id"));
                            save.put("name", proxy.getName());
                            save.put("localPort", proxy.getLocalPort());
                            save.put("remotePort", proxy.getRemotePort());
                            save.put("status", proxy.getStatus());
                            save.put("type", proxy.getType().name().toLowerCase(Locale.ROOT));
                            configStore.addProxy(save);
                            logger.info("客户端 {}-映射名 {}-公网端口 {} 已经同步到数据库", existClient.get("id"), proxy.getName(), proxy.getRemotePort());
                        }
                    } else {
                        logger.warn("该客户端公网端口「{}-{}」已经被注册", name, remotePort);
                    }

                });
            } else {
                logger.warn("「{}」 客户端已经被注册", name);
            }
        });
    }

    private static void registerDBClientConfig() {
        JSONArray clients = configStore.listClients();
        if (clients != null) {
            for (int i = 0; i < clients.length(); i++) {
                JSONObject client = clients.getJSONObject(i);
                String name = client.getString("name");
                ClientInfo clientInfo = new ClientInfo(client.getString("secretKey"));
                clientInfo.setClientId(client.getInt("id"));
                clientInfo.setName(client.getString("name"));
                runtimeState.registerClient(clientInfo);
                logger.info("Client {} 已注册", name);
            }
        }
    }

    private static void registerDBProxyConfig() {
        JSONArray proxies = configStore.listAllProxies();
        if (proxies != null) {
            for (int i = 0; i < proxies.length(); i++) {
                JSONObject proxy = proxies.getJSONObject(i);
                String secretKey = proxy.getString("secretKey");
                ProxyMapping proxyMapping = new ProxyMapping(
                        ProtocolType.getType(proxy.getString("type")),
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
        logger.debug("数据库表初始化完毕");
    }

    private static void createAuthTokenTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS auth_tokens (
                    token TEXT PRIMARY KEY,
                    uid INTEGER NOT NULL,
                    username TEXT NOT NULL,
                    expiredAt INTEGER NOT NULL,         -- unix 时间戳（秒）
                    createdAt INTEGER DEFAULT (strftime('%s','now')),
                    FOREIGN KEY (uid) REFERENCES users(id)
                );
                CREATE INDEX IF NOT EXISTS idx_auth_tokens_expires_at ON auth_tokens(expires_at);
                """;
        SQLiteUtils.createTable(sql);
    }

    private static void createUserTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL
                );
                """;
        SQLiteUtils.createTable(sql);
    }

    private static void createProxiesTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS proxies (
                    id          INTEGER PRIMARY KEY AUTOINCREMENT,  -- 自增主键
                    clientId   INTEGER NOT NULL,                    -- 所属客户端ID
                    name        TEXT NOT NULL,                      -- 代理名称
                    type        TEXT NOT NULL,                      -- 协议类型（如 "TCP"、"HTTP"）
                    localPort  INTEGER NOT NULL,                    -- 内网端口（如 3306）
                    remotePort INTEGER NOT NULL UNIQUE,             -- 远程服务端口（对外暴露的端口）
                    status      INTEGER NOT NULL DEFAULT 1,         -- 状态：1=开启，0=关闭
                    createdAt  TEXT DEFAULT (datetime('now')),      -- 创建时间
                    updatedAt  TEXT DEFAULT (datetime('now'))       -- 更新时间
                );
                """;
        SQLiteUtils.createTable(sql);
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
                """;
        SQLiteUtils.createTable(sql);
    }
}
