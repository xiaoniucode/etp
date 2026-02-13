package com.xiaoniucode.etp.server.web.service.impl;

import com.xiaoniucode.etp.server.manager.session.AgentSessionManager;
import com.xiaoniucode.etp.server.web.common.BizException;
import com.xiaoniucode.etp.server.web.controller.accesstoken.convert.AccessTokenConvert;
import com.xiaoniucode.etp.server.web.controller.accesstoken.response.AccessTokenDTO;
import com.xiaoniucode.etp.server.web.domain.AccessToken;
import com.xiaoniucode.etp.server.web.controller.accesstoken.request.BatchDeleteAccessTokenRequest;
import com.xiaoniucode.etp.server.web.repository.AccessTokenRepository;
import com.xiaoniucode.etp.server.web.service.AccessTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AccessTokenServiceImpl implements AccessTokenService {

    @Autowired
    private AccessTokenRepository accessTokenRepository;
    @Autowired
    private AgentSessionManager agentSessionManager;
    @Autowired
    private AccessTokenConvert accessTokenConvert;

    @Override
    public AccessToken create(AccessToken accessToken) {
        String token = UUID.randomUUID().toString().replaceAll("-", "");
        accessToken.setToken(token);
        return accessTokenRepository.save(accessToken);
    }

    @Override
    public List<AccessTokenDTO> findAll() {
        List<AccessToken> accessTokens = accessTokenRepository.findAll();
        return accessTokens.stream()
                .map(accessToken -> {
                    AccessTokenDTO dto = accessTokenConvert.toDTO(accessToken);
                    Integer onlineClient = agentSessionManager.getOnlineAgents(accessToken.getToken());
                    dto.setOnlineClient(onlineClient);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public AccessTokenDTO findById(Integer id) {
        AccessToken accessToken = accessTokenRepository.findById(id)
                .orElseThrow(() -> new BizException("访问令牌不存在"));
        AccessTokenDTO dto = accessTokenConvert.toDTO(accessToken);
        Integer onlineClient = agentSessionManager.getOnlineAgents(accessToken.getToken());
        dto.setOnlineClient(onlineClient);
        return dto;
    }

    @Override
    public AccessToken update(AccessToken accessToken) {
        AccessToken existingToken = accessTokenRepository.findById(accessToken.getId())
                .orElseThrow(() -> new BizException("访问令牌不存在"));
        existingToken.setName(accessToken.getName());
        // 不更新 token 字段
        existingToken.setMaxClient(accessToken.getMaxClient());
        return accessTokenRepository.save(existingToken);
    }

    @Override
    public void delete(Integer id) {
        accessTokenRepository.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(BatchDeleteAccessTokenRequest request) {
        List<Integer> ids = request.getIds();
        accessTokenRepository.deleteAllById(ids);
    }
}
