package com.xiaoniucode.etp.server.web.service;

import com.xiaoniucode.etp.server.web.controller.user.request.UpdatePasswordRequest;
import com.xiaoniucode.etp.server.web.domain.User;

public interface UserService {
    User getByUsername(String username);
    void register(User user);
    void deleteAll();
    void updatePassword(Integer userId, UpdatePasswordRequest req);
}
