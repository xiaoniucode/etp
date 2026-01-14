package com.xiaoniucode.etp.server.web.core.server;

import java.util.UUID;
import java.util.concurrent.*;

/**
 * session管理器
 * @author liuxin
 */
public final class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();
    // Session 超时时间：30 分钟
    private static final long SESSION_TIMEOUT = 30 * 60 * 1000L;
    // 定时清理过期 Session
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor();
    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

    private SessionManager() {
        cleaner.scheduleWithFixedDelay(this::cleanExpiredSessions, 5, 5, TimeUnit.MINUTES);
    }

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    public Session getSession(String sessionId, boolean create) {
        if (sessionId == null || sessionId.isEmpty()) {
            if (!create) {
                return null;
            }
            return createSession();
        }

        Session session = sessions.get(sessionId);
        if (session == null && create) {
            session = createSession();
            sessions.put(session.getId(), session);
        }
        if (session != null) {
            session.touch();
        }
        return session;
    }

    private Session createSession() {
        String id = UUID.randomUUID().toString().replace("-", "");
        Session session = new Session(id);
        sessions.put(id, session);
        return session;
    }

    public void removeSession(String sessionId) {
        Session session = sessions.remove(sessionId);
        if (session != null) {
            session.invalidate();
        }
    }

    private void cleanExpiredSessions() {
        long now = System.currentTimeMillis();
        sessions.entrySet().removeIf(entry -> {
            Session s = entry.getValue();
            return (now - s.getLastAccessedTime()) > SESSION_TIMEOUT;
        });
    }

    public void shutdown() {
        cleaner.shutdown();
    }
}
