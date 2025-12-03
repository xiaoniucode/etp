package cn.xilio.etp.server.web.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Session
 *
 * @author liuxin
 */
public class Session {
    /**
     * SessionID
     */
    private final String id;
    /**
     * Session创建时间
     */
    private final long createTime;
    /**
     * Session最后被访问时间
     */
    private volatile long lastAccessedTime;
    /**
     * 用于向Session存储数据
     */
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    public Session(String id) {
        this.id = id;
        long now = System.currentTimeMillis();
        this.createTime = now;
        this.lastAccessedTime = now;
    }

    public String getId() {
        return id;
    }

    public void touch() {
        this.lastAccessedTime = System.currentTimeMillis();
    }

    public long getLastAccessedTime() {
        return lastAccessedTime;
    }

    public void setAttribute(String key, Object value) {
        if (value == null) {
            attributes.remove(key);
        } else {
            attributes.put(key, value);
        }
    }

    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }

    public void removeAttribute(String key) {
        attributes.remove(key);
    }

    public void invalidate() {
        attributes.clear();
    }
}
