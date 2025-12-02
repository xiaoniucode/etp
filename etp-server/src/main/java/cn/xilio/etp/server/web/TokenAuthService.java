package cn.xilio.etp.server.web;

import cn.xilio.etp.common.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
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

    // ========== 静态定时任务：类加载即自动启动，每天凌晨3点清理过期token ==========
    static {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Token-Cleaner-Thread");
            t.setDaemon(true);
            return t;
        });

        // 计算距离今天凌晨3点的秒数
        long initDelay = Duration.between(LocalTime.now(), LocalTime.of(3, 0)).getSeconds();
        if (initDelay < 0) {
            /*如果已经过了凌晨3点，就明天3点*/
            initDelay += 24 * 60 * 60;
        }
        scheduler.scheduleAtFixedRate(() -> {
            try {
                cleanExpiredTokens();
                logger.debug("[TokenAuthService] 过期 token 已清理完成 - {} ", LocalTime.now());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }, initDelay, 24 * 60 * 60, TimeUnit.SECONDS);
        /*JVM关闭时优雅关闭线程池*/
        Runtime.getRuntime().addShutdownHook(new Thread(scheduler::shutdownNow));
    }

    public static JSONObject createToken(int userId, String username) {
        String token = UUID.randomUUID().toString().replaceAll("-", "");
        long expiresAt = System.currentTimeMillis() / 1000 + TOKEN_EXPIRE_SECONDS;
        String sql = "INSERT INTO auth_tokens (token, uid, username, expiredAt) VALUES (?, ?, ?, ?)";
        SQLiteUtils.insert(sql, token, userId, username, expiresAt);
        JSONObject res = new JSONObject();
        res.put("auth_token",token);
        res.put("expired_in",expiresAt);
        return res;
    }

    public static Integer validateToken(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }
        String sql = "SELECT uid FROM auth_tokens WHERE token = ? AND expiredAt > strftime('%s', 'now')";
        JSONObject one = SQLiteUtils.get(sql, token);
        if (one != null) {
            return one.getInt("uid");
        }
        return null;
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
        String sql = "DELETE FROM auth_tokens WHERE expires_at <= strftime('%s', 'now')";
        SQLiteUtils.delete(sql);
    }
}
