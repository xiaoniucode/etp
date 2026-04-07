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

import com.xiaoniucode.etp.server.generator.UUIDGenerator;
import com.xiaoniucode.etp.server.web.controller.accesstoken.convert.AccessTokenConvert;
import com.xiaoniucode.etp.server.web.controller.accesstoken.request.BatchDeleteAccessTokenRequest;
import com.xiaoniucode.etp.server.web.controller.accesstoken.request.CreateAccessTokenRequest;
import com.xiaoniucode.etp.server.web.controller.accesstoken.request.UpdateAccessTokenRequest;
import com.xiaoniucode.etp.server.web.controller.accesstoken.response.AccessTokenDTO;
import com.xiaoniucode.etp.server.web.entity.AccessToken;
import com.xiaoniucode.etp.server.web.repository.AccessTokenRepository;
import com.xiaoniucode.etp.server.web.service.AccessTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccessTokenServiceImpl implements AccessTokenService {
    @Autowired
    private AccessTokenRepository accessTokenRepository;
    @Autowired
    private UUIDGenerator uuidGenerator;

    @Override
    public AccessToken create(CreateAccessTokenRequest request) {
        AccessToken accessToken = AccessTokenConvert.INSTANCE.toEntity(request);
        String token = uuidGenerator.uuid32().toUpperCase();
        accessToken.setToken(token);
        return accessTokenRepository.save(accessToken);
    }

    @Override
    public List<AccessTokenDTO> findAll(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<AccessToken> tokenPage;
        if (keyword != null && !keyword.isEmpty()) {
            tokenPage = accessTokenRepository.findByKeyword(keyword, pageable);
        } else {
            tokenPage = accessTokenRepository.findAll(pageable);
        }

        List<AccessToken> tokens = tokenPage.getContent();
        return AccessTokenConvert.INSTANCE.toDTOList(tokens);
    }

    @Override
    public AccessTokenDTO findById(Integer id) {
        AccessToken token = accessTokenRepository.findById(id).orElse(null);
        if (token == null) {
            return null;
        }
        return AccessTokenConvert.INSTANCE.toDTO(token);
    }

    @Override
    public void update(UpdateAccessTokenRequest request) {
        AccessToken existingToken = accessTokenRepository.findById(request.getId()).orElse(null);
        if (existingToken != null) {
            AccessToken updatedToken = AccessTokenConvert.INSTANCE.toEntity(request);

            updatedToken.setId(request.getId());
            updatedToken.setToken(existingToken.getToken());
            updatedToken.setCreatedAt(existingToken.getCreatedAt());
            accessTokenRepository.save(updatedToken);
        }
    }

    @Override
    public void delete(Integer id) {
        accessTokenRepository.deleteById(id);
    }

    @Override
    public void deleteBatch(BatchDeleteAccessTokenRequest request) {
        List<Integer> ids = request.getIds();
        if (ids != null && !ids.isEmpty()) {
            accessTokenRepository.deleteAllById(ids);
        }
    }
}
