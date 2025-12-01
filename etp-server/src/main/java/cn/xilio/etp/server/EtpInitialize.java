package cn.xilio.etp.server;

import cn.xilio.etp.core.protocol.ProtocolType;
import cn.xilio.etp.server.config.ClientInfo;
import cn.xilio.etp.server.config.ProxyMapping;
import cn.xilio.etp.server.manager.RuntimeState;
import cn.xilio.etp.server.web.ConfigService;
import cn.xilio.etp.server.web.ConfigStore;
import cn.xilio.etp.server.web.SQLiteUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author liuxin
 */
public final class EtpInitialize {
    private static Logger logger = LoggerFactory.getLogger(EtpInitialize.class);
    private final static ConfigStore configStore = new ConfigStore();
    private final static RuntimeState runtimeState = RuntimeState.get();

    public static void init() {
        //如果数据库和表不存在则创建
        initTable();
        //注册所有客户端配置
        registerClientConfig();
        //注册所有端口映射配置
        registerProxyConfig();
    }
    private static void registerClientConfig() {
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

    private static void registerProxyConfig() {
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
    private static void initTable() {
        logger.debug("开始初始化数据库表");
        createClient();
        createProxyMapping();
        logger.debug("数据库表初始化完毕");
    }

    private static void createProxyMapping() {
        String sql = """
                CREATE TABLE IF NOT EXISTS proxies (
                    id          INTEGER PRIMARY KEY AUTOINCREMENT,  -- 自增主键
                    clientId   INTEGER NOT NULL,                     -- 所属客户端ID
                    name        TEXT NOT NULL,                        -- 代理名称
                    type        TEXT NOT NULL,                        -- 协议类型（如 "TCP"、"HTTP"）
                    localPort  INTEGER NOT NULL,                     -- 内网端口（如 3389）
                    remotePort INTEGER NOT NULL,                     -- 远程服务端口（对外暴露的端口）
                    status      INTEGER NOT NULL DEFAULT 1,           -- 状态：1=开启，0=关闭
                    createdAt  TEXT DEFAULT (datetime('now')),       -- 创建时间
                    updatedAt  TEXT DEFAULT (datetime('now'))        -- 更新时间
                );
                """;
        SQLiteUtils.createTable(sql);
    }

    private static void createClient() {
        String sql = """
                CREATE TABLE IF NOT EXISTS clients (
                    id         INTEGER PRIMARY KEY AUTOINCREMENT,   -- 自增ID
                    name       TEXT    NOT NULL UNIQUE,             -- 客户端名称，唯一
                    secretKey  TEXT    NOT NULL,                    -- 密钥
                    createdAt TEXT    DEFAULT (datetime('now')),   -- 创建时间
                    updatedAt TEXT    DEFAULT (datetime('now'))    -- 更新时间
                );
                """;
        SQLiteUtils.createTable(sql);
    }
}
