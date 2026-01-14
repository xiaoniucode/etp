package com.xiaoniucode.etp.server.web.dao;

import com.xiaoniucode.etp.server.web.core.orm.Jdbc;
import com.xiaoniucode.etp.server.web.core.orm.JdbcFactory;

public abstract class BaseDao {
    protected final Jdbc jdbc;

    protected BaseDao() {
        this.jdbc = JdbcFactory.getJdbc();
    }
    public Jdbc getJdbc() {
        return jdbc;
    }
}