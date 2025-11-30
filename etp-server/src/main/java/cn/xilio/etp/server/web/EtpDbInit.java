package cn.xilio.etp.server.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author liuxin
 */
public final class EtpDbInit {
    private static Logger logger = LoggerFactory.getLogger(EtpDbInit.class);

    /**
     * 只有不存在的时候才会创建表
     */
    public static void initTable() {
        logger.debug("开始初始化数据库表");
        createClient();
        createProxyMapping();
        //todo 将Toml中的配置信息加载到数据库
        //todo 将数据库中的信息设置到Config，需要梳理优化
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
