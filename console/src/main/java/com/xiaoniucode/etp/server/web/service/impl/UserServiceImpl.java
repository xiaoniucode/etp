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

import com.xiaoniucode.etp.server.web.common.exception.BizException;
import com.xiaoniucode.etp.server.web.dto.user.UserInfoDTO;
import com.xiaoniucode.etp.server.web.entity.SysUserDO;
import com.xiaoniucode.etp.server.web.param.user.UserPasswordUpdateParam;
import com.xiaoniucode.etp.server.web.repository.UserRepository;
import com.xiaoniucode.etp.server.web.service.UserService;
import com.xiaoniucode.etp.server.web.service.converter.SysUserConvert;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private SysUserConvert sysUserConvert;

    @Override
    public UserInfoDTO getByUsername(String username) {
        SysUserDO sysUserDO = userRepository.findByUsername(username).orElseThrow(() -> new BizException("用户不存在"));
        UserInfoDTO infoDTO = sysUserConvert.toInfoDTO(sysUserDO);
        infoDTO.setRoles(List.of(sysUserDO.getRole()));
        return infoDTO;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void updatePassword(String username, UserPasswordUpdateParam req) {
        SysUserDO user = userRepository.findByUsername(username).orElseThrow(() -> new BizException(401, "用户不存在"));

        // 检查旧密码是否正确
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BizException(400, "旧密码错误");
        }

        // 检查新密码和确认密码是否一致
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new BizException(400, "新密码和确认密码不一致");
        }

        // 对新密码进行加密
        String encodedPassword = passwordEncoder.encode(req.getNewPassword());

        // 更新用户密码
        user.setPassword(encodedPassword);
        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUserDO sysUser = userRepository.findByUsername(username).orElseThrow(() -> new BizException("用户不存在"));
        List<GrantedAuthority> authorities = new ArrayList<>();
        return User.builder()
                .username(sysUser.getUsername())
                .password(sysUser.getPassword())
                .authorities(authorities)
                .build();
    }
}
