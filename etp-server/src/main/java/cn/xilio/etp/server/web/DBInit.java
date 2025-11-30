package cn.xilio.etp.server.web;

public class DBInit {

    public static void init() {
        createClient();
        createProxyMapping();
    }

    private static void createProxyMapping() {

    }

    private static void createClient() {
        String sql = """
                CREATE TABLE IF NOT EXISTS client (
                    id         INTEGER PRIMARY KEY AUTOINCREMENT,   -- 自增ID
                    name       TEXT    NOT NULL UNIQUE,             -- 客户端名称，唯一
                    secretKey  TEXT    NOT NULL,                    -- 密钥
                    created_at TEXT    DEFAULT (datetime('now')),   -- 创建时间
                    updated_at TEXT    DEFAULT (datetime('now'))    -- 更新时间
                );
                """;
        SQLiteUtils.createTable(sql);
    }
}
