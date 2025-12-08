package com.xiaoniucode.etp.server.web.transaction;

/**
 * 事务回调接口，保证业务逻辑在事务中执行
 *
 * @author liuxin
 */
@FunctionalInterface
public interface TransactionCallback<T> {
    T doInTransaction() throws Exception;
}
