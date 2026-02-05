package com.xiaoniucode.etp.server.web.task;

import com.xiaoniucode.etp.server.web.manager.CaptchaEntry;
import com.xiaoniucode.etp.server.web.manager.CaptchaManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CaptchaCleanupTask {
    private final Logger logger = LoggerFactory.getLogger(CaptchaCleanupTask.class);
    @Autowired
    private CaptchaManager captchaManager;

    @Scheduled(fixedRate = 60000)
    public void cleanupCaptcha() {
        logger.debug("验证码清理任务");
        Map<String, CaptchaEntry> caches = captchaManager.getCaches();
        if (caches != null) {
            long now = System.currentTimeMillis();
            caches.entrySet().removeIf(entry -> entry.getValue().expireAt() <= now);
            logger.debug("清理完成，剩余验证码数量: {}", caches.size());
        }
    }
}
