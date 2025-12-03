package cn.xilio.etp.server.web.manager;

import cn.xilio.etp.common.StringUtils;
import cn.xilio.etp.server.web.SQLiteUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 登录认证令牌管理
 *
 * @author liuxin
 */
public class TokenAuthService {
    private final static Logger logger = LoggerFactory.getLogger(TokenAuthService.class);
    private static final long TOKEN_EXPIRE_SECONDS = 24 * 60 * 60;
    // 清理时间点（小时, 分钟）
    private static final LocalTime CLEANUP_TIME = LocalTime.of(1, 0);

    static {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Token-Cleaner-Thread");
            t.setDaemon(true);
            return t;
        });

        //计算下一次执行的延迟秒数
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = LocalDateTime.of(LocalDate.now(), CLEANUP_TIME);

        // 如果今天已经过了 CLEANUP_TIME 就安排到明天同一时间
        if (now.isAfter(nextRun) || now.toLocalTime().equals(CLEANUP_TIME)) {
            nextRun = nextRun.plusDays(1);
        }

        long initDelay = Duration.between(now, nextRun).getSeconds();
        logger.debug("[TokenAuthService] 过期Token清理任务已启动，每天 {} 执行，{} 秒后运行（{}）",
                CLEANUP_TIME,
                initDelay,
                nextRun.toLocalTime());

        scheduler.scheduleAtFixedRate(() -> {
            try {
                cleanExpiredTokens();
                logger.debug("[TokenAuthService] 过期 token 已清理完成 - {}", LocalTime.now());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }, initDelay, 24 * 60 * 60, TimeUnit.SECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(scheduler::shutdownNow));
    }

    public static JSONObject createToken(int userId, String username) {
        String token = UUID.randomUUID().toString().replaceAll("-", "");
        long expiresAt = System.currentTimeMillis() / 1000 + TOKEN_EXPIRE_SECONDS;
        String sql = "INSERT INTO auth_tokens (token, uid, username, expiredAt) VALUES (?, ?, ?, ?)";
        SQLiteUtils.insert(sql, token, userId, username, expiresAt);
        JSONObject res = new JSONObject();
        res.put("userId", userId);
        res.put("username", username);
        res.put("auth_token", token);
        res.put("expired_in", expiresAt);
        return res;
    }

    public static JSONObject validateToken(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }
        String sql = "SELECT uid,username FROM auth_tokens WHERE token = ? AND expiredAt > strftime('%s', 'now')";
        return SQLiteUtils.get(sql, token);

    }

    public static void invalidateToken(String token) {
        if (StringUtils.hasText(token)) {
            String sql = "DELETE FROM auth_tokens WHERE token = ?";
            SQLiteUtils.delete(sql, token);
        }
    }

    /**
     * 刷新 token（相当于重新登录，生成新 token，旧的自动过期或可删）
     */
    public static JSONObject refreshToken(String oldToken) {
        String sql = "SELECT uid, username FROM auth_tokens WHERE token = ?";
        JSONObject result = SQLiteUtils.get(sql, oldToken);
        if (result != null) {
            // 旧 Token 立即失效
            invalidateToken(oldToken);
            return createToken(result.getInt("uid"), result.getString("username"));
        }
        return null;
    }

    /**
     * 清理过期token
     */
    public static void cleanExpiredTokens() {
        String sql = "DELETE FROM auth_tokens WHERE expiredAt <= strftime('%s', 'now')";
        SQLiteUtils.delete(sql);
    }
}
