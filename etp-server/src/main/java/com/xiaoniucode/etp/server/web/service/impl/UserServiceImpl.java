package com.xiaoniucode.etp.server.web.service.impl;

import com.xiaoniucode.etp.server.web.common.BizException;
import com.xiaoniucode.etp.server.web.controller.user.request.UpdatePasswordRequest;
import com.xiaoniucode.etp.server.web.domain.User;
import com.xiaoniucode.etp.server.web.repository.UserRepository;
import com.xiaoniucode.etp.server.web.service.AuthService;
import com.xiaoniucode.etp.server.web.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository usersRepository;

    @Autowired
    private AuthService authTokenService;



    @Override
    public User getByUsername(String username) {
        return usersRepository.findByUsername(username);
    }

    @Override
    public void register(User user) {
        usersRepository.save(user);
    }

    @Override
    public void deleteAll() {
        usersRepository.deleteAll();
    }


    @Override
    @Transactional(rollbackOn = Exception.class)
    public void updatePassword(Integer userId, UpdatePasswordRequest req) {
        int id = userId;
        User user = usersRepository.findById(id).orElse(null);
        if (user == null) {
            throw new BizException(401, "用户不存在");
        }
    }
}
