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
package com.xiaoniucode.etp.server.web.service.impl;
import com.xiaoniucode.etp.server.web.common.BizException;
import com.xiaoniucode.etp.server.web.entity.SysUserDO;
import com.xiaoniucode.etp.server.web.param.user.UserPasswordUpdateParam;
import com.xiaoniucode.etp.server.web.repository.UserRepository;
import com.xiaoniucode.etp.server.web.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
@Service
public class UserServiceImpl implements UserService, UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Override
    public SysUserDO getByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    @Override
    public void register(SysUserDO user) {
        userRepository.save(user);
    }
    @Override
    public void deleteAll() {
        userRepository.deleteAll();
    }
    @Override
    @Transactional(rollbackOn = Exception.class)
    public void updatePassword(Integer userId, UserPasswordUpdateParam req) {
        int id = userId;
        SysUserDO user = userRepository.findById(id).orElse(null);
        if (user == null) {
            throw new BizException(401, "用户不存在");
        }
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUserDO sysUser = userRepository.findByUsername(username);
        if (sysUser == null) {
            throw new BizException("用户不存在");
        }
        List<GrantedAuthority> authorities = new ArrayList<>();
        return User.builder()
                .username(sysUser.getUsername())
                .password(sysUser.getPassword())
                .authorities(authorities)
                .build();
    }
}
