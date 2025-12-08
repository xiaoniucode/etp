package com.xiaoniucode.etp.server.web;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * @author liuxin
 */
public final class SQLiteUtils {
    private final static Logger logger = LoggerFactory.getLogger(SQLiteUtils.class);
    private static final String DB_URL = "jdbc:sqlite:etp.db";

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
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // 设置参数
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int columnCount = meta.getColumnCount();

                while (rs.next()) {
                    JSONObject row = new JSONObject();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = meta.getColumnLabel(i);
                        Object value = rs.getObject(i);
                        row.put(columnName, value == null ? JSONObject.NULL : value);
                    }
                    array.put(row);
                }
            }
            logger.debug("SQL 查询成功 → {} 条记录", array.length());
        } catch (SQLException e) {
            logger.error("SQL 查询失败：{}  →  {}", sql, e.getMessage(), e);
        }
        return array;
    }

    public static long insert(String sql, Object... params) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            int affected = pstmt.executeUpdate();
            if (affected == 0) {
                logger.warn("插入失败：{}", sql);
                return -1L;
            }
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    logger.debug("插入成功，ID = {}", id);
                    return id;
                }
            }
            return -1L;
        } catch (SQLException e) {
            logger.error("插入失败：{} → {}", sql, e.getMessage(), e);
            return -1L;
        }
    }

    public static int update(String sql, Object... params) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                logger.debug("更新成功，影响 {} 行，SQL: {}", affected, sql);
            } else {
                logger.info("更新未影响到任何行，SQL: {}", sql);
            }
            return affected;

        } catch (SQLException e) {
            logger.error("更新失败：{} → {}", sql, e.getMessage(), e);
            return 0;
        }
    }

    public static int delete(String sql, Object... params) {
        return update(sql, params);
    }

    public static void dropTable(String tableName) {
        String sql = "DROP TABLE IF EXISTS " + tableName;
        execute(sql);
        logger.info("表 {} 已成功删除", tableName);
    }

    private static void execute(String sql) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.debug("SQL 执行成功：{}", sql.trim());
        } catch (SQLException e) {
            logger.error("SQL 执行失败：{}  →  {}", sql.trim(), e.getMessage(), e);
        }
    }
}
