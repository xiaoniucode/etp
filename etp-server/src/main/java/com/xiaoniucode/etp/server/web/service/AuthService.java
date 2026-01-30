package com.xiaoniucode.etp.server.web.service;

import com.xiaoniucode.etp.server.web.controller.auth.request.LoginRequest;
import com.xiaoniucode.etp.server.web.domain.LoginToken;
import jakarta.validation.Valid;

public interface AuthService {
    LoginToken createToken(int userId, String username);

    LoginToken validateToken(String token);

    void invalidateToken(String token);

    LoginToken login(@Valid LoginRequest param);
}
