package com.xiaoniucode.etp.server.web.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
@Component
public class CaptchaManager {
    private final Logger logger = LoggerFactory.getLogger(CaptchaManager.class);
    private final Map<String, CaptchaEntry> cache = new ConcurrentHashMap<>();

    public Map<String, CaptchaEntry> getCaches() {
        return cache;
    }
    /**
     * 存放验证码，返回一个唯一的 captchaId
     */
    public String add(String code, int expireSeconds) {
        String captchaId = UUID.randomUUID().toString().replaceAll("-", "");
        cache.put(captchaId, new CaptchaEntry(code.toUpperCase(), System.currentTimeMillis() + expireSeconds * 1000L));
        return captchaId;
    }

    /**
     * 校验并删除（一次性）
     */
    public boolean verifyAndRemove(String captchaId, String userInput) {
        if (captchaId == null || userInput == null) {
            return false;
        }
        CaptchaEntry entry = cache.remove(captchaId);
        if (entry == null) {
            return false;
        }
        return entry.code().equalsIgnoreCase(userInput.trim());
    }

    /**
     * 只校验（不删除），允许重复校验
     */
    public boolean verify(String captchaId, String userInput) {
        CaptchaEntry entry = cache.get(captchaId);
        if (entry == null) {
            return false;
        }
        return entry.code().equalsIgnoreCase(userInput.trim());
    }
}
