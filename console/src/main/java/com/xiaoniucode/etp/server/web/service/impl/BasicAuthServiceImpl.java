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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BasicAuthServiceImpl implements BasicAuthService {
    @Autowired
    private BasicAuthRepository basicAuthRepository;
    @Autowired
    private BasicUserRepository basicUserRepository;
    @Autowired
    private BasicAuthConvert basicAuthConvert;

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
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUser(HttpUserAddParam request) {
        BasicAuthDO basicAuthDO = basicAuthRepository.findById(request.getProxyId())
                .orElseThrow(() -> new BizException("Basic Auth 配置不存在"));
        BasicUserDO basicUserDO = basicAuthConvert.toUserDO(request);
        basicUserRepository.save(basicUserDO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(HttpUserUpdateParam request) {
        BasicUserDO basicUserDO = basicUserRepository.findById(request.getId())
                .orElseThrow(() -> new BizException("用户不存在"));
        basicAuthConvert.updateUserDO(basicUserDO, request);
        basicUserRepository.save(basicUserDO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        basicUserRepository.deleteById(id);
    }
}

