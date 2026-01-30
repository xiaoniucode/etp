package com.xiaoniucode.etp.server.web.controller.captcha;

import com.xiaoniucode.etp.server.web.common.Ajax;
import com.xiaoniucode.etp.server.web.common.CaptchaGenerator;
import com.xiaoniucode.etp.server.web.controller.captcha.resp.Captcha;
import com.xiaoniucode.etp.server.web.manager.CaptchaManager;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

/**
 * 验证码接口控制器
 *
 * @author liuxin
 */
@RestController
@RequestMapping("/captcha")
public class CaptchaController {
    @GetMapping
    public Ajax generateCaptcha(HttpSession session) {
        try {
            CaptchaGenerator generator = new CaptchaGenerator();
            String code = generator.generateCaptcha();
            String captchaId = CaptchaManager.put(code, 120);
            session.setAttribute("captchaId", captchaId);
            return Ajax.success(new Captcha(captchaId,code));
        } catch (Exception e) {
            throw new RuntimeException("生成验证码失败", e);
        }
    }
}
