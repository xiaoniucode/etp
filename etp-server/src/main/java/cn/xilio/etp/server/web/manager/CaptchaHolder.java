package cn.xilio.etp.server.web.manager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 验证码管理
 *
 * @author liuxin
 */
public class CaptchaHolder {
    private static final Map<String, CaptchaEntry> cache = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService CLEANER = Executors.newSingleThreadScheduledExecutor();

    static {
        // 每分钟清理一次过期验证码
        CLEANER.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            cache.entrySet().removeIf(entry -> entry.getValue().expireAt <= now);
        }, 1, 1, TimeUnit.MINUTES);
    }

    /**
     * @param code     验证码文字
     * @param expireAt 过期时间戳
     */
    private record CaptchaEntry(String code, long expireAt) {
    }

    /**
     * 存放验证码，返回一个唯一的 captchaId
     */
    public static String put(String code, int expireSeconds) {
        String captchaId = UUID.randomUUID().toString().replaceAll("-", "");
        cache.put(captchaId, new CaptchaEntry(code.toUpperCase(), System.currentTimeMillis() + expireSeconds * 1000L));
        return captchaId;
    }

    /**
     * 校验并删除（一次性）
     */
    public static boolean verifyAndRemove(String captchaId, String userInput) {
        if (captchaId == null || userInput == null) {
            return false;
        }
        CaptchaEntry entry = cache.remove(captchaId);
        if (entry == null) {
            return false;
        }
        return entry.code.equalsIgnoreCase(userInput.trim());
    }

    /**
     * 只校验（不删除），允许重复校验
     */
    public static boolean verify(String captchaId, String userInput) {
        CaptchaEntry entry = cache.get(captchaId);
        if (entry == null) {
            return false;
        }
        return entry.code.equalsIgnoreCase(userInput.trim());
    }
}
