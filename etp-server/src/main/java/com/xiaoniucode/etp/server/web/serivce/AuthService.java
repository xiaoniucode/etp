package com.xiaoniucode.etp.server.web.serivce;

import com.xiaoniucode.etp.common.utils.StringUtils;
import com.xiaoniucode.etp.server.web.dao.DaoFactory;
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

public class AuthService {
    private final static Logger logger = LoggerFactory.getLogger(AuthService.class);
    /**
     * Token有效期 默认1天
     */
    private static final long TOKEN_EXPIRE_SECONDS = 24 * 60 * 60;

    /**
     * 清理时间点（小时:分钟）
     */
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

        // 如果今天已经过了CLEANUP_TIME 就安排到明天同一时间
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

    /**
     * 创建认证令牌
     */
    public JSONObject createToken(int userId, String username) {
        String token = UUID.randomUUID().toString().replaceAll("-", "");
        long expiresAt = System.currentTimeMillis() / 1000 + TOKEN_EXPIRE_SECONDS;

        JSONObject tokenData = new JSONObject();
        tokenData.put("token", token);
        tokenData.put("uid", userId);
        tokenData.put("username", username);
        tokenData.put("expiredAt", expiresAt);

        DaoFactory.INSTANCE.getAuthTokenDao().insert(tokenData);

        JSONObject result = new JSONObject();
        result.put("userId", userId);
        result.put("username", username);
        result.put("auth_token", token);
        result.put("expired_in", expiresAt);

        logger.debug("创建认证令牌成功");
        return result;
    }

    /**
     * 验证令牌有效性
     */
    public JSONObject validateToken(String token) {
        if (!StringUtils.hasText(token)) {
            logger.debug("令牌验证失败 - 令牌为空");
            return null;
        }

        JSONObject tokenInfo = DaoFactory.INSTANCE.getAuthTokenDao().getByToken(token);
        if (tokenInfo != null) {
            logger.debug("令牌验证成功 -  用户: {}", tokenInfo.getString("username"));
        } else {
            logger.debug("令牌验证失败，令牌无效或已过期");
        }

        return tokenInfo;
    }

    /**
     * 使令牌失效
     */
    public void invalidateToken(String token) {
        if (StringUtils.hasText(token)) {
            boolean deleted = DaoFactory.INSTANCE.getAuthTokenDao().deleteByToken(token);
            if (deleted) {
                logger.debug("令牌已失效");
            } else {
                logger.debug("令牌失效失败，令牌不存在" );
            }
        }
    }

    /**
     * 刷新令牌
     */
    public JSONObject refreshToken(String oldToken) {
        if (!StringUtils.hasText(oldToken)) {
            return null;
        }

        JSONObject oldTokenInfo = DaoFactory.INSTANCE.getAuthTokenDao().getByTokenIgnoreExpiry(oldToken);
        if (oldTokenInfo != null) {
            // 使旧令牌失效
            invalidateToken(oldToken);

            // 创建新令牌
            return createToken(oldTokenInfo.getInt("uid"), oldTokenInfo.getString("username"));
        }

        logger.debug("令牌刷新失败，旧令牌无效");
        return null;
    }

    /**
     * 清理过期令牌
     */
    public static int cleanExpiredTokens() {
        int deletedCount = DaoFactory.INSTANCE.getAuthTokenDao().deleteExpiredTokens();
        logger.debug("清理过期令牌完成 - 删除数量: {}", deletedCount);
        return deletedCount;
    }

    public static AuthService getInstance() {
        return AuthTokenServiceHolder.INSTANCE;
    }

    private static class AuthTokenServiceHolder {
        private static final AuthService INSTANCE = new AuthService();
    }
}