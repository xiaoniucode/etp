package com.xiaoniucode.etp.server.web.controller.user;

import com.xiaoniucode.etp.server.web.common.Ajax;
import com.xiaoniucode.etp.server.web.controller.auth.request.LoginRequest;
import com.xiaoniucode.etp.server.web.controller.user.request.UpdatePasswordRequest;
import com.xiaoniucode.etp.server.web.service.AuthService;
import com.xiaoniucode.etp.server.web.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理接口控制器
 *
 * @author liuxin
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;
    @GetMapping("/profile")
    public Ajax getProfile() {
        return Ajax.success();
    }

    @PutMapping("/password")
    public Ajax updatePassword(@Valid @RequestBody UpdatePasswordRequest param) {
        Integer userId = 1;
        userService.updatePassword(userId, param);
        return Ajax.success("ok");
    }
}
