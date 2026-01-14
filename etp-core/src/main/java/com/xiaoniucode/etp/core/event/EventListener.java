package com.xiaoniucode.etp.core.event;

/**
 * 事件监听器接口
 *
 * @param <T> 监听的事件类型
 * @author liuxin
 */
@FunctionalInterface
public interface EventListener<T extends EventObject> {
    
    /**
     * 处理事件
     * @param event 接收到的事件
     */
    void onEvent(T event);
}