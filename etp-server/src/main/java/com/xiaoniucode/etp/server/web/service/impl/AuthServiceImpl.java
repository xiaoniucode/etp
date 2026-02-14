package com.xiaoniucode.etp.server.web.service.impl;

import com.xiaoniucode.etp.server.web.common.BizException;
import com.xiaoniucode.etp.server.web.controller.auth.request.LoginRequest;
import com.xiaoniucode.etp.server.web.controller.auth.response.LoginResponse;
import com.xiaoniucode.etp.server.web.manager.CaptchaManager;
import com.xiaoniucode.etp.server.web.security.TokenUtil;
import com.xiaoniucode.etp.server.web.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private TokenUtil tokenUtil;
    @Autowired
    private CaptchaManager captchaManager;

    @Override
    public LoginResponse login(LoginRequest request) {
        captchaManager.verifyAndRemove(request.getCaptchaId(), request.getCode());
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenUtil.generateToken(request.getUsername());
        return new LoginResponse(token);
    }
}
