package com.xiaoniucode.etp.client.manager;

import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.core.notify.EventListener;

/**
 * Disruptor 事件总线管理器
 */
public class EventBusManager {
    private static volatile EventBus eventBus;
    private static final Object lock = new Object();
    
    private EventBusManager() {}
    
    public static EventBus getEventBus() {
        if (eventBus == null) {
            synchronized (lock) {
                if (eventBus == null) {
                    eventBus = new EventBus();
                }
            }
        }
        return eventBus;
    }
    
    public static <T> void register(EventListener<T> listener, Class<T> eventType) {
        getEventBus().register(listener, eventType);
    }
    
    public static <T> void register(EventListener<T> listener) {
        getEventBus().register(listener);
    }
    
    public static void unregister(EventListener<?> listener) {
        getEventBus().unregister(listener);
    }
    
    public static void publishSync(Object event) {
        getEventBus().publishSync(event);
    }
    
    public static void publishAsync(Object event) {
        getEventBus().publishAsync(event);
    }
    
    public static void shutdown() {
        if (eventBus != null) {
            eventBus.shutdown();
            eventBus = null;
        }
    }
}