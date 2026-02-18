package com.xiaoniucode.etp.server.web.service;

import com.xiaoniucode.etp.server.web.controller.auth.request.LoginRequest;
import com.xiaoniucode.etp.server.web.controller.auth.response.LoginResponse;
import jakarta.validation.Valid;

public interface AuthService {

    LoginResponse login(@Valid LoginRequest request);
}
