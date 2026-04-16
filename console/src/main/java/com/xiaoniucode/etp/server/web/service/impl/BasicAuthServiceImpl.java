/*
 *    Copyright 2026 xiaoniucode
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.xiaoniucode.etp.server.web.service.impl;

import com.xiaoniucode.etp.core.domain.BasicAuthConfig;
import com.xiaoniucode.etp.core.domain.HttpUser;
import com.xiaoniucode.etp.server.registry.ProxyManager;
import com.xiaoniucode.etp.server.web.common.exception.BizException;
import com.xiaoniucode.etp.server.web.dto.basicauth.BasicAuthDetailDTO;
import com.xiaoniucode.etp.server.web.entity.BasicAuthDO;
import com.xiaoniucode.etp.server.web.entity.BasicUserDO;
import com.xiaoniucode.etp.server.web.param.basicauth.BasicAuthUpdateParam;
import com.xiaoniucode.etp.server.web.param.basicauth.httpuser.HttpUserAddParam;
import com.xiaoniucode.etp.server.web.param.basicauth.httpuser.HttpUserUpdateParam;
import com.xiaoniucode.etp.server.web.repository.BasicAuthRepository;
import com.xiaoniucode.etp.server.web.repository.BasicUserRepository;
import com.xiaoniucode.etp.server.web.service.BasicAuthService;
import com.xiaoniucode.etp.server.web.service.converter.BasicAuthConvert;
import com.xiaoniucode.etp.server.web.support.tx.TransactionHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class BasicAuthServiceImpl implements BasicAuthService {
    @Autowired
    private BasicAuthRepository basicAuthRepository;
    @Autowired
    private BasicUserRepository basicUserRepository;
    @Autowired
    private BasicAuthConvert basicAuthConvert;
    @Autowired
    private ProxyManager proxyManager;
    @Autowired
    private TransactionHelper transactionHelper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public BasicAuthDetailDTO getByProxyId(String proxyId) {
        BasicAuthDO basicAuthDO = basicAuthRepository.findById(proxyId)
                .orElseThrow(() -> new BizException("Basic Auth 配置不存在"));
        List<BasicUserDO> basicUserDOS = basicUserRepository.findByProxyId(proxyId);
        return basicAuthConvert.toDetailDTO(basicAuthDO, basicUserDOS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(BasicAuthUpdateParam request) {
        String proxyId = request.getProxyId();
        BasicAuthDO basicAuthDO = basicAuthRepository.findById(proxyId)
                .orElseThrow(() -> new BizException("Basic Auth 配置不存在"));
        basicAuthDO.setEnabled(request.getEnabled());
        basicAuthRepository.save(basicAuthDO);

        transactionHelper.afterCommit(() -> {
            proxyManager.findById(proxyId).ifPresent(proxyConfig -> {
                BasicAuthConfig ba = proxyConfig.getOrCreateBasicAuthConfig();
                ba.setEnabled(basicAuthDO.getEnabled());
                proxyConfig.setBasicAuth(ba);
            });
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUser(HttpUserAddParam request) {
        BasicAuthDO basicAuthDO = basicAuthRepository.findById(request.getProxyId())
                .orElseThrow(() -> new BizException("Basic Auth 配置不存在"));
        boolean exists = basicUserRepository.existsByUsername(request.getUsername());
        if (exists) {
            throw new BizException("用户名已存在");
        }
        BasicUserDO basicUserDO = basicAuthConvert.toUserDO(request);
        String encode = passwordEncoder.encode(request.getPassword());
        basicUserDO.setPassword(encode);
        basicUserRepository.save(basicUserDO);
        transactionHelper.afterCommit(() -> {
            proxyManager.findById(basicAuthDO.getProxyId()).ifPresent(proxyConfig -> {
                BasicAuthConfig ba = proxyConfig.getOrCreateBasicAuthConfig();
                HttpUser httpUser = HttpUser.of(basicUserDO.getUsername(), encode);
                ba.addUser(httpUser);
            });
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(HttpUserUpdateParam param) {
        String proxyId = param.getProxyId();
        BasicUserDO basicUserDO = basicUserRepository.findById(param.getId())
                .orElseThrow(() -> new BizException("用户不存在"));

        String oldUsername = basicUserDO.getUsername();
        String encode = passwordEncoder.encode(param.getPassword());
        boolean usernameChanged = !Objects.equals(param.getUsername(), basicUserDO.getUsername());
        if (!usernameChanged && param.getPassword() == null) {
            return;
        }

        if (usernameChanged) {
            boolean exists = basicUserRepository
                    .existsByProxyIdAndUsernameAndIdNot(
                            proxyId,
                            param.getUsername(),
                            param.getId()
                    );
            if (exists) {
                throw new BizException("用户名已存在");
            }
        }

        basicUserDO.setPassword(encode);
        basicAuthConvert.updateUserDO(basicUserDO, param);
        basicUserRepository.save(basicUserDO);

        transactionHelper.afterCommit(() -> {
            proxyManager.findById(basicUserDO.getProxyId()).ifPresent(proxyConfig -> {
                BasicAuthConfig ba = proxyConfig.getOrCreateBasicAuthConfig();
                HttpUser httpUser = HttpUser.of(basicUserDO.getUsername(), encode);
                ba.removeUser(oldUsername);
                ba.addUser(httpUser);
            });
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        BasicUserDO basicUserDO = basicUserRepository.findById(id).orElseThrow(() -> new BizException("用户不存在"));
        String proxyId = basicUserDO.getProxyId();
        basicUserRepository.deleteById(id);
        transactionHelper.afterCommit(() -> {
            proxyManager.findById(proxyId).ifPresent(proxyConfig -> {
                BasicAuthConfig ba = proxyConfig.getOrCreateBasicAuthConfig();
                ba.removeUser(basicUserDO.getUsername());
            });
        });
    }
}

