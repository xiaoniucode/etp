package cn.xilio.etp.server.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteUtils {
    private final static Logger logger = LoggerFactory.getLogger(SQLiteUtils.class);
    private static final String DB_URL = "jdbc:sqlite:etp.db";
    public static void createTable(String sql) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
