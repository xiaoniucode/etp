package com.xiaoniucode.etp.server.web.core.orm.transaction;

/**
 * 事务回调接口，保证业务逻辑在事务中执行
 *
 * @author liuxin
 */
@FunctionalInterface
public interface TransactionCallback<T> {
    /**
     * 在事务中执行业务逻辑
     *
     * @return 执行成功返回结果，如果不需要返回值，直接返回null即可
     * @throws Exception 异常
     */
    T doInTransaction() throws Exception;
}
