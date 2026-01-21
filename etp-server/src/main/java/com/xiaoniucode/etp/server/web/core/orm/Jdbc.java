package com.xiaoniucode.etp.server.web.core.orm;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Jdbc通用操作类
 *
 * @author liuxin
 */
public final class Jdbc {
    private static final Pattern PARAM_PATTERN = Pattern.compile(":(\\w+)");

    private final String url;
    private Connection connection;

    private Jdbc(String url) {
        this.url = url;
    }

    public static Jdbc create(String url) {
        return new Jdbc(url);
    }

    private Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(url);
        }
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void execute(String sql) {
        execute(sql, new Object[0]);
    }

    public void execute(String sql, Object... params) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            setParameters(stmt, params);
            stmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException("SQL执行失败: " + sql + " → " + e.getMessage(), e);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
            }
        }
    }

    /**
     * 创建查询构建器
     */
    public QueryBuilder query(String sql) {
        return new QueryBuilder(this, sql);
    }

    /**
     * 创建更新构建器
     */
    public UpdateBuilder update(String sql) {
        return new UpdateBuilder(this, sql);
    }

    /**
     * 创建插入构建器
     */
    public InsertBuilder insert(String sql) {
        return new InsertBuilder(this, sql);
    }

    /**
     * 关闭连接
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            // 忽略关闭异常
        }
    }

    private void setParameters(PreparedStatement stmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
    }

    /**
     * 解析SQL中的冒号参数，替换为问号，并返回参数顺序
     */
    private static SqlParseResult parseSql(String sql, Map<String, Object> paramMap) {
        List<Object> paramValues = new ArrayList<>();

        // 查找所有冒号参数
        Matcher matcher = PARAM_PATTERN.matcher(sql);
        StringBuilder parsedSql = new StringBuilder();

        while (matcher.find()) {
            String paramName = matcher.group(1);

            if (!paramMap.containsKey(paramName)) {
                throw new RuntimeException("参数未绑定: " + paramName);
            }

            paramValues.add(paramMap.get(paramName));
            matcher.appendReplacement(parsedSql, "?");
        }

        matcher.appendTail(parsedSql);

        return new SqlParseResult(parsedSql.toString(), paramValues.toArray());
    }

    private record SqlParseResult(String sql, Object[] params) {
    }

    /**
     * 查询构建器
     */
    public static class QueryBuilder {
        private final Jdbc jdbc;
        private final String originalSql;
        private final Map<String, Object> paramMap;

        private QueryBuilder(Jdbc jdbc, String sql) {
            this.jdbc = jdbc;
            this.originalSql = sql;
            this.paramMap = new HashMap<>();
        }

        public QueryBuilder bind(String key, Object value) {
            paramMap.put(key, value);
            return this;
        }

        public JSONObject one() {
            JSONArray results = list();
            return results.isEmpty() ? null : results.getJSONObject(0);
        }

        public JSONArray list() {
            JSONArray results = new JSONArray();
            SqlParseResult parseResult = parseSql(originalSql, paramMap);

            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                conn = jdbc.getConnection();
                stmt = conn.prepareStatement(parseResult.sql);
                jdbc.setParameters(stmt, parseResult.params);
                rs = stmt.executeQuery();
                ResultSetMetaData meta = rs.getMetaData();
                int columnCount = meta.getColumnCount();

                while (rs.next()) {
                    JSONObject row = new JSONObject();
                    for (int i = 1; i <= columnCount; i++) {
                        Object value = rs.getObject(i);
                        row.put(meta.getColumnLabel(i), value == null ? JSONObject.NULL : value);
                    }
                    results.put(row);
                }
            } catch (SQLException e) {
                throw new RuntimeException("SQL查询失败: " + parseResult.sql + " → " + e.getMessage(), e);
            } finally {
                // 不要关闭连接，因为连接可能是事务管理器提供的
                try {
                    if (rs != null) {
                        rs.close();
                    }
                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (SQLException e) {
                    // 忽略关闭异常
                }
            }

            return results;
        }
    }

    /**
     * 更新构建器
     */
    public static class UpdateBuilder {
        private final Jdbc jdbc;
        private final String originalSql;
        private final Map<String, Object> paramMap;

        private UpdateBuilder(Jdbc jdbc, String sql) {
            this.jdbc = jdbc;
            this.originalSql = sql;
            this.paramMap = new HashMap<>();
        }

        /**
         * 绑定参数（键值对）
         */
        public UpdateBuilder bind(String key, Object value) {
            paramMap.put(key, value);
            return this;
        }

        /**
         * 执行更新操作
         */
        public int execute() {
            SqlParseResult parseResult = parseSql(originalSql, paramMap);

            Connection conn = null;
            PreparedStatement stmt = null;
            try {
                conn = jdbc.getConnection();
                stmt = conn.prepareStatement(parseResult.sql);
                jdbc.setParameters(stmt, parseResult.params);
                return stmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("SQL更新失败: " + parseResult.sql + " → " + e.getMessage(), e);
            } finally {
                // 不要关闭连接，因为连接可能是事务管理器提供的
                try {
                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (SQLException e) {
                    // 忽略关闭异常
                }
            }
        }
    }

    /**
     * 插入构建器
     */
    public static class InsertBuilder {
        private final Jdbc jdbc;
        private final String originalSql;
        private final Map<String, Object> paramMap;

        private InsertBuilder(Jdbc jdbc, String sql) {
            this.jdbc = jdbc;
            this.originalSql = sql;
            this.paramMap = new HashMap<>();
        }

        /**
         * 绑定参数
         */
        public InsertBuilder bind(String key, Object value) {
            paramMap.put(key, value);
            return this;
        }

        /**
         * 执行插入操作
         */
        public int execute() {
            SqlParseResult parseResult = parseSql(originalSql, paramMap);

            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet generatedKeys = null;
            try {
                conn = jdbc.getConnection();
                stmt = conn.prepareStatement(parseResult.sql, Statement.RETURN_GENERATED_KEYS);
                jdbc.setParameters(stmt, parseResult.params);
                stmt.executeUpdate();
                
                generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
                return 0;
            } catch (SQLException e) {
                throw new RuntimeException("SQL插入失败: " + parseResult.sql + " → " + e.getMessage(), e);
            } finally {
                // 不要关闭连接，因为连接可能是事务管理器提供的
                try {
                    if (generatedKeys != null) {
                        generatedKeys.close();
                    }
                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (SQLException e) {
                    // 忽略关闭异常
                }
            }
        }
    }
}