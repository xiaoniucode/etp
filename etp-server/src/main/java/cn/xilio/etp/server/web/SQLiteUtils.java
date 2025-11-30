package cn.xilio.etp.server.web;

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

    /**
     * 创建表
     */
    public static void createTable(String sql) {
        execute(sql);
    }

    /**
     * 查询单条记录
     *
     * @param sql    带 ? 占位符的 SQL
     * @param params 参数数组（可变参数）
     * @return JSONObject 或 null（没查到）
     */
    public static JSONObject get(String sql, Object... params) {
        JSONArray result = list(sql, params);
        if (result.isEmpty()) {
            return null;
        }
        return result.getJSONObject(0);
    }

    /**
     * 查询多条记录
     *
     * @param sql    带 ? 占位符的 SQL
     * @param params 参数数组（可变参数）
     * @return JSONArray
     */
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

    /**
     * 根据表名删除表
     *
     * @param tableName 表名
     */
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
