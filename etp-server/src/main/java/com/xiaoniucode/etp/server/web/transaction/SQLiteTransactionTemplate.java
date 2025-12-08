package com.xiaoniucode.etp.server.web.transaction;

/**
 * 事务模版，提供便捷的操作
 *
 * @author liuxin
 */
public class SQLiteTransactionTemplate {

    private final SQLiteTransactionManager txManager = new SQLiteTransactionManager();

    public <T> T execute(TransactionCallback<T> callback) {
        txManager.begin();
        try {
            T result = callback.doInTransaction();
            txManager.commit();
            return result;
        } catch (Exception e) {
            txManager.rollback();
            throw new RuntimeException(e);
        }
    }
}
