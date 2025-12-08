package com.xiaoniucode.etp.server.web.transaction;

import java.sql.Connection;

/**
 * 保证每一个线程的事务独立
 *
 * @author liuxin
 */
public final class SQLiteConnectionHolder {
    private static final ThreadLocal<Connection> CURRENT = new ThreadLocal<>();

    public static void set(Connection conn) {
        CURRENT.set(conn);
    }

    public static Connection get() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }
}
