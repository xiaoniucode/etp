package com.xiaoniucode.etp.server.web.core.orm.transaction;

import com.xiaoniucode.etp.common.Constants;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * 事务管理器，管理事务的开启、提交、会滚、连接关闭以及线程资源的释放
 *
 * @author liuxin
 */
public class JdbcTransactionManager {
    public void begin() {
        try {
            Connection conn = DriverManager.getConnection(Constants.SQLITE_DB_URL);
            conn.setAutoCommit(false);
            JdbcConnectionHolder.set(conn);
        } catch (Exception e) {
            throw new RuntimeException("无法开启事务", e);
        }
    }

    public void commit() {
        Connection conn = JdbcConnectionHolder.get();
        if (conn != null) {
            try {
                conn.commit();
            } catch (Exception e) {
                throw new RuntimeException("事务提交失败", e);
            } finally {
                close();
            }
        }
    }

    public void rollback() {
        Connection conn = JdbcConnectionHolder.get();
        if (conn != null) {
            try {
                conn.rollback();
            } catch (Exception e) {
                throw new RuntimeException("事务回滚失败", e);
            } finally {
                close();
            }
        }
    }

    /**
     * 关闭数据库连接
     * 释放当前线程数据库连接资源
     */
    private void close() {
        Connection conn = JdbcConnectionHolder.get();
        JdbcConnectionHolder.clear();
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
