package com.xiaoniucode.etp.server.web.manager;

import com.xiaoniucode.etp.server.web.common.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
     * 校验并删除
     */
    public void verifyAndRemove(String captchaId, String code) {
        if (captchaId == null || code == null) {
            throw new BizException("输入验证码为空");
        }
        CaptchaEntry entry = cache.remove(captchaId);
        verifyEntry(entry, captchaId, code);
    }

    /**
     * 只校验不删除，允许重复校验
     */
    public void verify(String captchaId, String code) {
        if (captchaId == null || code == null) {
            throw new BizException("输入验证码为空");
        }
        CaptchaEntry entry = cache.get(captchaId);
        verifyEntry(entry, captchaId, code);
    }

    /**
     * 验证验证码条目
     */
    private void verifyEntry(CaptchaEntry entry, String captchaId, String code) {
        if (entry == null) {
            throw new BizException("验证码不存在");
        }
        // 检查是否过期
        if (entry.expireAt() < System.currentTimeMillis()) {
            logger.debug("验证码已过期: {}", captchaId);
            throw new BizException("验证码已过期");
        }
        //检查是否正确
        boolean result = entry.code().equalsIgnoreCase(code.trim());
        logger.debug("验证码校验结果: {}, 验证码ID: {}", result, captchaId);
        if (!result) {
            throw new BizException("验证码不正确");
        }
    }
}
