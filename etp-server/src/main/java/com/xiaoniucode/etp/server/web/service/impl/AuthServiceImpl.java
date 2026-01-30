package com.xiaoniucode.etp.server.web.service.impl;

import com.xiaoniucode.etp.server.web.common.BizException;
import com.xiaoniucode.etp.server.web.common.DigestUtil;
import com.xiaoniucode.etp.server.web.controller.auth.request.LoginRequest;
import com.xiaoniucode.etp.server.web.domain.User;
import com.xiaoniucode.etp.server.web.repository.UserRepository;
import com.xiaoniucode.etp.server.web.service.AuthService;
import com.xiaoniucode.etp.server.web.domain.LoginToken;
import com.xiaoniucode.etp.server.web.repository.LoginTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;
    @Override
    public LoginToken login(LoginRequest req) {

        String username = req.getUsername();
        String password = req.getPassword();
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new BizException(401, "用户不存在！");
        }
        //检查密码
        if (!DigestUtil.encode(password, username).equals(user.getPassword())) {
            throw new BizException(401, "密码错误");
        }
        //创建登录令牌
        return createToken(user.getId(), username);
    }
    @Override
    public LoginToken createToken(int userId, String username) {
        LoginToken token = new LoginToken();
        token.setUid(userId);
        // 注意：username 字段可能不存在于 AuthTokens 实体中，需要根据实际情况调整
        return token;
    }

    @Override
    public LoginToken validateToken(String token) {
        return null;
    }

    @Override
    public void invalidateToken(String token) {

    }
}
