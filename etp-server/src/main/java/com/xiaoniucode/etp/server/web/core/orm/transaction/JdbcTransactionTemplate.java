package com.xiaoniucode.etp.server.web.core.orm.transaction;

/**
 * 事务模版，提供便捷的操作
 *
 * @author liuxin
 */
public class JdbcTransactionTemplate {

    private final JdbcTransactionManager txManager = new JdbcTransactionManager();

    /**
     * 执行事务
     *
     * @param callback 回调接口，用于执行业务逻辑
     * @param <T>      业务执行成功返回值类型
     * @return 业务返回结果
     */
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
