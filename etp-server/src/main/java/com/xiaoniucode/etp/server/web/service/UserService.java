package com.xiaoniucode.etp.server.web.service;

import com.xiaoniucode.etp.server.web.controller.user.request.UpdatePasswordRequest;
import com.xiaoniucode.etp.server.web.domain.SysUser;

public interface UserService {
    SysUser getByUsername(String username);
    void register(SysUser user);
    void deleteAll();
    void updatePassword(Integer userId, UpdatePasswordRequest req);
}
