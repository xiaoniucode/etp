--必须分号分隔，否则脚本执行会出错
-- 系统设置表
CREATE TABLE IF NOT EXISTS settings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    key TEXT NOT NULL UNIQUE,       -- 设置键
    value TEXT NOT NULL             -- 设置值
);
CREATE INDEX IF NOT EXISTS idx_settings_key ON settings(key);

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL
);

-- 认证令牌表
CREATE TABLE IF NOT EXISTS auth_tokens (
    token TEXT PRIMARY KEY,
    uid INTEGER NOT NULL,
    username TEXT NOT NULL,
    expiredAt INTEGER NOT NULL,
    createdAt INTEGER DEFAULT (datetime('now')),
    FOREIGN KEY (uid) REFERENCES users(id)
);
CREATE INDEX IF NOT EXISTS idx_auth_tokens_expiredAt ON auth_tokens(expiredAt);

-- 客户端表
CREATE TABLE IF NOT EXISTS clients (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,  -- 自增ID
    name       TEXT    NOT NULL UNIQUE,            -- 客户端名称
    secretKey  TEXT    NOT NULL UNIQUE,            -- 密钥
    createdAt TEXT    DEFAULT (datetime('now')),   -- 创建时间
    updatedAt TEXT    DEFAULT (datetime('now'))    -- 更新时间
);
CREATE INDEX IF NOT EXISTS idx_clients_name_secretkey ON clients (name, secretKey);

-- 代理表
CREATE TABLE IF NOT EXISTS proxies (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,  -- 自增主键
    clientId         INTEGER NOT NULL,                   -- 所属客户端ID
    name             TEXT NOT NULL UNIQUE,               -- 代理名称
    type             TEXT NOT NULL,                      -- 协议类型（如 "TCP"、"HTTP"）
    source           INTEGER NOT NULL DEFAULT 0,         -- 注册类型（1：自动注册、0手动注册）
    localIP          TEXT NOT NULL,                      -- 内网IP
    localPort        INTEGER NOT NULL,                   -- 内网端口
    remotePort       INTEGER,                            -- 远程服务端口
    status           INTEGER NOT NULL DEFAULT 1,         -- 状态：1=开启，0=关闭
    createdAt        TEXT DEFAULT (datetime('now')),     -- 创建时间
    updatedAt        TEXT DEFAULT (datetime('now'))      -- 更新时间
);

-- 代理域名表
CREATE TABLE IF NOT EXISTS proxy_domains (
    id        INTEGER PRIMARY KEY AUTOINCREMENT,
    proxyId   INTEGER NOT NULL,
    domain    TEXT NOT NULL,
    createdAt TEXT DEFAULT (datetime('now')),
    UNIQUE(domain),
    FOREIGN KEY (proxyId) REFERENCES proxies(id) ON DELETE CASCADE
);