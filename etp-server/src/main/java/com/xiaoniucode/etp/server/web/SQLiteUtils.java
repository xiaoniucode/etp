package com.xiaoniucode.etp.server.web;

import com.xiaoniucode.etp.common.Constants;
import com.xiaoniucode.etp.server.web.transaction.SQLiteConnectionHolder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * SQLite 工具类
 *
 * @author liuxin
 */
public final class SQLiteUtils {
    private final static Logger logger = LoggerFactory.getLogger(SQLiteUtils.class);

    /**
     * 获取连接：如果有事务连接，则返回事务连接
     */
    private static Connection getConnection() throws SQLException {
        Connection conn = SQLiteConnectionHolder.get();
        if (conn != null) {
            return conn;
        }
        return DriverManager.getConnection(Constants.SQLITE_DB_URL);
    }

    /**
     * 是否是事务连接
     */
    private static boolean isTransactional(Connection conn) {
        return conn == SQLiteConnectionHolder.get();
    }

    /**
     * 关闭非事务连接
     */
    private static void closeIfNonTransactional(Connection conn) {
        if (!isTransactional(conn)) {
            try {
                conn.close();
            } catch (Exception ignored) {
            }
        }
    }

    public static void createTable(String sql) {
        execute(sql);
    }

    public static JSONObject get(String sql, Object... params) {
        JSONArray result = list(sql, params);
        if (result.isEmpty()) {
            return null;
        }
        return result.getJSONObject(0);
    }

    public static JSONArray list(String sql, Object... params) {
        JSONArray array = new JSONArray();
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

                for (int i = 0; i < params.length; i++) {
                    pstmt.setObject(i + 1, params[i]);
                }

                try (ResultSet rs = pstmt.executeQuery()) {
                    ResultSetMetaData meta = rs.getMetaData();
                    int columnCount = meta.getColumnCount();

                    while (rs.next()) {
                        JSONObject row = new JSONObject();
                        for (int i = 1; i <= columnCount; i++) {
                            Object value = rs.getObject(i);
                            row.put(meta.getColumnLabel(i), value == null ? JSONObject.NULL : value);
                        }
                        array.put(row);
                    }
                }
            }
            logger.debug("SQL 查询成功 → {} 条记录", array.length());
        } catch (SQLException e) {
            throw new RuntimeException("SQL 查询失败：" + sql + " → " + e.getMessage(), e);
        } finally {
            closeIfNonTransactional(conn);
        }
        return array;
    }

    public static int insert(String sql, Object... params) {
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                for (int i = 0; i < params.length; i++) {
                    pstmt.setObject(i + 1, params[i]);
                }

                int affected = pstmt.executeUpdate();
                if (affected == 0) {
                    throw new RuntimeException("插入失败：" + sql);
                }

                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        logger.debug("插入成功，ID = {}", id);
                        return id;
                    } else {
                        throw new RuntimeException("插入成功但未返回主键：" + sql);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("SQL 插入失败：" + sql + " → " + e.getMessage(), e);
        } finally {
            closeIfNonTransactional(conn);
        }
    }

    public static int update(String sql, Object... params) {
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

                for (int i = 0; i < params.length; i++) {
                    pstmt.setObject(i + 1, params[i]);
                }

                int affected = pstmt.executeUpdate();
                logger.debug("更新完成，影响 {} 行，SQL: {}", affected, sql);
                return affected;
            }
        } catch (SQLException e) {
            throw new RuntimeException("SQL 更新失败：" + sql + " → " + e.getMessage(), e);
        } finally {
            closeIfNonTransactional(conn);
        }
    }

    public static int delete(String sql, Object... params) {
        return update(sql, params);
    }

    public static void dropTable(String tableName) {
        execute("DROP TABLE IF EXISTS " + tableName);
        logger.info("表 {} 已成功删除", tableName);
    }

    private static void execute(String sql) {
        Connection conn = null;
        try {
            conn = getConnection();
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
                logger.debug("SQL 执行成功：{}", sql.trim());
            }
        } catch (SQLException e) {
            throw new RuntimeException("SQL 执行失败：" + sql.trim() + " → " + e.getMessage(), e);
        } finally {
            closeIfNonTransactional(conn);
        }
    }
}
