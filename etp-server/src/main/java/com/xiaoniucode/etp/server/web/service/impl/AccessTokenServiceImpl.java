package com.xiaoniucode.etp.server.web.service.impl;

import com.xiaoniucode.etp.server.web.common.BizException;
import com.xiaoniucode.etp.server.web.domain.AccessToken;
import com.xiaoniucode.etp.server.web.repository.AccessTokenRepository;
import com.xiaoniucode.etp.server.web.service.AccessTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AccessTokenServiceImpl implements AccessTokenService {

    @Autowired
    private AccessTokenRepository accessTokenRepository;

    @Override
    public AccessToken create(AccessToken accessToken) {
        // 自动生成 UUID 格式的 token，去掉下划线
        String token = UUID.randomUUID().toString().replaceAll("-", "");
        accessToken.setToken(token);
        return accessTokenRepository.save(accessToken);
    }

    @Override
    public List<AccessToken> findAll() {
        return accessTokenRepository.findAll();
    }

    @Override
    public AccessToken findById(Integer id) {
        return accessTokenRepository.findById(id)
                .orElseThrow(() -> new BizException("访问令牌不存在"));
    }

    @Override
    public AccessToken update(AccessToken accessToken) {
        AccessToken existingToken = findById(accessToken.getId());
        existingToken.setName(accessToken.getName());
        // 不更新 token 字段
        existingToken.setMaxClient(accessToken.getMaxClient());
        return accessTokenRepository.save(existingToken);
    }

    @Override
    public void delete(Integer id) {
        accessTokenRepository.deleteById(id);
    }
}
