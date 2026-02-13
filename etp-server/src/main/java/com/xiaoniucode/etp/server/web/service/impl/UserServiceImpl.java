package com.xiaoniucode.etp.server.web.service.impl;

import com.xiaoniucode.etp.server.web.common.BizException;
import com.xiaoniucode.etp.server.web.controller.user.request.UpdatePasswordRequest;
import com.xiaoniucode.etp.server.web.domain.SysUser;
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
    public SysUser getByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public void register(SysUser user) {
        userRepository.save(user);
    }

    @Override
    public void deleteAll() {
        userRepository.deleteAll();
    }


    @Override
    @Transactional(rollbackOn = Exception.class)
    public void updatePassword(Integer userId, UpdatePasswordRequest req) {
        int id = userId;
        SysUser user = userRepository.findById(id).orElse(null);
        if (user == null) {
            throw new BizException(401, "用户不存在");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser sysUser = userRepository.findByUsername(username);
        if (sysUser == null) {
            throw new BizException("用户不存在");
        }
        List<GrantedAuthority> authorities = new ArrayList<>();
//        if (sysUser.getRole() != null) {
//            authorities.add(new SimpleGrantedAuthority(sysUser.getRole()));
//        }
        return User.builder()
                .username(sysUser.getUsername())
                .password(sysUser.getPassword())
                .authorities(authorities)
                .build();
    }
}
