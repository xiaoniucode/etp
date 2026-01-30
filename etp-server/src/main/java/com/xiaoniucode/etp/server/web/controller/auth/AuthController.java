package com.xiaoniucode.etp.server.web.controller.auth;

import com.xiaoniucode.etp.common.utils.StringUtils;
import com.xiaoniucode.etp.server.web.common.Ajax;
import com.xiaoniucode.etp.server.web.controller.auth.request.LoginRequest;
import com.xiaoniucode.etp.server.web.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 认证管理接口控制器
 *
 * @author liuxin
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Ajax login(@Valid @RequestBody LoginRequest param) {
        return Ajax.success(authService.login(param));
    }

    @PostMapping("/logout")
    public Ajax logout(@RequestHeader("Authorization") String auth) {
        if (StringUtils.hasText(auth) && auth.startsWith("Bearer ")) {
            authService.invalidateToken(auth.substring(7));
        }
        return Ajax.success();
    }
}
