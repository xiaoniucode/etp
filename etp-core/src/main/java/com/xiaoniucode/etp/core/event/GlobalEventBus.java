package com.xiaoniucode.etp.core.event;

public class GlobalEventBus {
    private static final EventBus INSTANCE = new EventBus();
    
    public static EventBus get() {
        return INSTANCE;
    }
    
    private GlobalEventBus() {}
}