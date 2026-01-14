package com.xiaoniucode.etp.core.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 事件总线
 * @author liuxin
 */
public class EventBus {
    private final Logger logger = LoggerFactory.getLogger(EventBus.class);
    private final Map<Class<?>, List<EventListener<?>>> listeners;
    private final Executor executor;
    private volatile boolean shutdown = false;
    private String identifier;

    public EventBus() {
        this("default");
    }

    public EventBus(String identifier) {
        this.identifier = identifier;
        this.listeners = new ConcurrentHashMap<>();
        this.executor = Executors.newCachedThreadPool();
    }

    /**
     * 发布事件（同步）
     * 在当前线程中同步处理所有监听器
     *
     * @param event 要发布的事件
     */
    public <T extends EventObject> void publishSync(T event) {
        checkShutdown();

        Class<?> eventType = event.getClass();
        List<EventListener<?>> eventListeners = listeners.get(eventType);

        if (eventListeners != null && !eventListeners.isEmpty()) {
            for (EventListener<?> listener : eventListeners) {
                @SuppressWarnings("unchecked")
                EventListener<T> typedListener = (EventListener<T>) listener;
                try {
                    typedListener.onEvent(event);
                } catch (Exception e) {
                    logger.error("事件监听器处理异常", e);
                }
            }
        }
    }

    /**
     * 发布事件（异步）
     * 在异步线程中处理所有监听器
     *
     * @param event 要发布的事件
     */
    public <T extends EventObject> void publishAsync(T event) {
        checkShutdown();
        executor.execute(() -> publishSync(event));
    }

    /**
     * 订阅事件
     *
     * @param eventClass 事件类
     * @param listener   事件监听器
     */
    public <T extends EventObject> void subscribe(Class<T> eventClass, EventListener<T> listener) {
        checkShutdown();

        listeners.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>())
                .add(listener);
    }

    /**
     * 取消订阅
     *
     * @param eventClass 事件类
     * @param listener   要移除的监听器
     */
    public <T extends EventObject> void unsubscribe(Class<T> eventClass, EventListener<T> listener) {
        checkShutdown();

        List<EventListener<?>> eventListeners = listeners.get(eventClass);
        if (eventListeners != null) {
            eventListeners.remove(listener);
            if (eventListeners.isEmpty()) {
                listeners.remove(eventClass);
            }
        }
    }

    /**
     * 关闭事件总线
     * 等待所有异步事件处理完成
     */
    public void shutdown() {
        if (!shutdown) {
            shutdown = true;
            if (executor instanceof ExecutorService) {
                ((ExecutorService) executor).shutdown();
            }
        }
    }

    /**
     * 立即关闭事件总线
     * 不等待异步事件处理完成
     */
    public void shutdownNow() {
        if (!shutdown) {
            shutdown = true;
            if (executor instanceof ExecutorService) {
                ((ExecutorService) executor).shutdownNow();
            }
        }
    }

    /**
     * 检查事件总线是否已关闭
     */
    private void checkShutdown() {
        if (shutdown) {
            throw new IllegalStateException("事件总线已关闭");
        }
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}