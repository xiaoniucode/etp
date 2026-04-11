/*
 *    Copyright 2026 xiaoniucode
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http:
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.xiaoniucode.etp.server.web.controller;
import com.xiaoniucode.etp.server.web.common.Ajax;
import com.xiaoniucode.etp.server.web.dto.auth.LoginDTO;
import com.xiaoniucode.etp.server.web.param.auth.LoginParam;
import com.xiaoniucode.etp.server.web.security.SecurityUtils;
import com.xiaoniucode.etp.server.web.service.AuthService;
import com.xiaoniucode.etp.server.web.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;
    @Autowired
    private UserService userService;
    @PostMapping("login")
    public Ajax login(@Valid @RequestBody LoginParam param) {
        LoginDTO response = authService.login(param);
        return Ajax.success(response);
    }
    @GetMapping("info")
    public Ajax info() {
        String username = SecurityUtils.getCurrentUsername();
        return Ajax.success(userService.getByUsername(username));
    }
}
