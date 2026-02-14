package com.xiaoniucode.etp.server.web.controller.auth;

import com.xiaoniucode.etp.server.web.common.Ajax;
import com.xiaoniucode.etp.server.web.controller.auth.request.LoginRequest;
import com.xiaoniucode.etp.server.web.controller.auth.response.LoginResponse;
import com.xiaoniucode.etp.server.web.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证管理接口控制器
 *
 * @author liuxin
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("login")
    public Ajax login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Ajax.success(response);
    }

}
