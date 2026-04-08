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
import com.xiaoniucode.etp.server.generator.UUIDGenerator;
import com.xiaoniucode.etp.server.web.dto.auth.AccessTokenDTO;
import com.xiaoniucode.etp.server.web.param.accesstoken.AccessTokenBatchDeleteParam;
import com.xiaoniucode.etp.server.web.param.accesstoken.AccessTokenCreateParam;
import com.xiaoniucode.etp.server.web.param.accesstoken.AccessTokenUpdateParam;
import com.xiaoniucode.etp.server.web.service.converter.AccessTokenConvert;
import com.xiaoniucode.etp.server.web.entity.AccessTokenDO;
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
    public AccessTokenDTO create(AccessTokenCreateParam request) {
        AccessTokenDO accessToken = AccessTokenConvert.INSTANCE.toEntity(request);
        String token = uuidGenerator.uuid32().toUpperCase();
        accessToken.setToken(token);
        AccessTokenDO save = accessTokenRepository.save(accessToken);
        return AccessTokenConvert.INSTANCE.toDTO(save);
    }
    @Override
    public List<AccessTokenDTO> findAll(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<AccessTokenDO> tokenPage;
        if (keyword != null && !keyword.isEmpty()) {
            tokenPage = accessTokenRepository.findByKeyword(keyword, pageable);
        } else {
            tokenPage = accessTokenRepository.findAll(pageable);
        }
        List<AccessTokenDO> tokens = tokenPage.getContent();
        return AccessTokenConvert.INSTANCE.toDTOList(tokens);
    }
    @Override
    public AccessTokenDTO findById(Integer id) {
        AccessTokenDO token = accessTokenRepository.findById(id).orElse(null);
        if (token == null) {
            return null;
        }
        return AccessTokenConvert.INSTANCE.toDTO(token);
    }
    @Override
    public void update(AccessTokenUpdateParam request) {
        AccessTokenDO existingToken = accessTokenRepository.findById(request.getId()).orElse(null);
        if (existingToken != null) {
            AccessTokenDO updatedToken = AccessTokenConvert.INSTANCE.toEntity(request);
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
    public void deleteBatch(AccessTokenBatchDeleteParam request) {
        List<Integer> ids = request.getIds();
        if (ids != null && !ids.isEmpty()) {
            accessTokenRepository.deleteAllById(ids);
        }
    }
}
