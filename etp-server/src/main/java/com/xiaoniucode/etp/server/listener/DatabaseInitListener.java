package com.xiaoniucode.etp.server.listener;

import com.xiaoniucode.etp.core.event.EventListener;
import com.xiaoniucode.etp.core.event.GlobalEventBus;
import com.xiaoniucode.etp.server.config.ConfigHelper;
import com.xiaoniucode.etp.server.event.DatabaseInitEvent;
import com.xiaoniucode.etp.server.event.TunnelBindEvent;
import com.xiaoniucode.etp.server.web.core.orm.JdbcFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/**
 * 当隧道开启成功且管理面板是开启状态时初始化数据库表
 */
public class DatabaseInitListener implements EventListener<TunnelBindEvent> {
    private final Logger logger = LoggerFactory.getLogger(DatabaseInitListener.class);
    private static final String SCHEMA_PATH = "schema/etp-sqlite.sql";

    @Override
    public void onEvent(TunnelBindEvent event) {
        if (ConfigHelper.get().getDashboard().getEnable()) {
            logger.debug("开始初始化数据库表");
            initDatabase();
            logger.debug("数据库表初始化完毕");
            GlobalEventBus.get().publishAsync(new DatabaseInitEvent());
        }
    }

    private void initDatabase() {
        try {
            //从资源文件中读取SQL语句
            String sqlContent = readSqlFile(SCHEMA_PATH);
            // 分割并执行每个SQL语句
            executeSqlStatements(sqlContent);
        } catch (Exception e) {
            logger.error("数据库表初始化失败", e);
            throw new RuntimeException("数据库表初始化失败", e);
        }
    }

    private void executeSqlStatements(String sqlContent) {
        // 分割SQL语句
        String[] statements = sqlContent.split(";");
        for (String statement : statements) {
            logger.debug("执行SQL语句: {}", statement);
            JdbcFactory.getJdbc().execute(statement);
        }
    }

    /**
     * 加载资源文件
     */
    private String readSqlFile(String filePath) throws IOException {
        // 使用ClassLoader
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath)) {
            assert inputStream != null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                // 读取文件内容
                return reader.lines().collect(Collectors.joining("\n"));
            }
        }
    }
}