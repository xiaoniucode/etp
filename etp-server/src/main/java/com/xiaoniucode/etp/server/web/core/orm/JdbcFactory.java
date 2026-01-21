package com.xiaoniucode.etp.server.web.core.orm;

import com.xiaoniucode.etp.common.Constants;
import com.xiaoniucode.etp.server.web.core.orm.transaction.JdbcConnectionHolder;

import java.sql.Connection;

public class JdbcFactory {
    private JdbcFactory() {
    }

    public static Jdbc getJdbc() {
        Connection connection = JdbcConnectionHolder.get();
        Jdbc jdbc = Jdbc.create(Constants.SQLITE_DB_URL);
        if (connection != null) {
            jdbc.setConnection(connection);
        }
        return jdbc;
    }
}
