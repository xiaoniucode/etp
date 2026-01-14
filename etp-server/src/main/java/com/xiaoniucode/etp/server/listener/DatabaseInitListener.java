package com.xiaoniucode.etp.server.listener;

import com.xiaoniucode.etp.core.event.EventListener;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.event.TunnelBindEvent;
import com.xiaoniucode.etp.server.web.core.orm.JdbcFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 当隧道开启成功且管理面板是开启状态时初始化数据库表
 */
public class DatabaseInitListener implements EventListener<TunnelBindEvent> {
    private final Logger logger = LoggerFactory.getLogger(DatabaseInitListener.class);

    @Override
    public void onEvent(TunnelBindEvent event) {
        if (AppConfig.get().getDashboard().getEnable()) {
            logger.debug("开始初始化数据库表");
            createAuthTokenTable();
            createUserTable();
            createClientTable();
            createProxiesTable();
            createSystemSettingsTable();
            logger.debug("数据库表初始化完毕");
        }
    }

    private void createSystemSettingsTable() {
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

    private void createAuthTokenTable() {
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

    private void createUserTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL
                );
                """;
        JdbcFactory.getJdbc().execute(sql);
    }

    private void createProxiesTable() {
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

    private void createClientTable() {
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
