package com.xiaoniucode.etp.server.web.core.orm;

import com.xiaoniucode.etp.common.Constants;
import com.xiaoniucode.etp.server.web.core.orm.transaction.JdbcConnectionHolder;

import java.sql.Connection;

public class JdbcFactory {
    private static final Jdbc INSTANCE = Jdbc.create(Constants.SQLITE_DB_URL);

    private JdbcFactory() {
    }

    public static Jdbc getJdbc() {
        Connection connection = JdbcConnectionHolder.get();
        if (connection != null) {
            INSTANCE.setConnection(connection);
        }
        return INSTANCE;
    }
}
