package com.xiaoniucode.etp.server.web.service.impl;

import com.xiaoniucode.etp.server.config.domain.AccessTokenInfo;
import com.xiaoniucode.etp.server.manager.AccessTokenManager;
import com.xiaoniucode.etp.server.manager.session.AgentSessionManager;
import com.xiaoniucode.etp.server.web.common.BizException;
import com.xiaoniucode.etp.server.web.controller.accesstoken.convert.AccessTokenConvert;
import com.xiaoniucode.etp.server.web.controller.accesstoken.request.CreateAccessTokenRequest;
import com.xiaoniucode.etp.server.web.controller.accesstoken.request.UpdateAccessTokenRequest;
import com.xiaoniucode.etp.server.web.controller.accesstoken.response.AccessTokenDTO;
import com.xiaoniucode.etp.server.web.entity.AccessToken;
import com.xiaoniucode.etp.server.web.controller.accesstoken.request.BatchDeleteAccessTokenRequest;
import com.xiaoniucode.etp.server.web.repository.AccessTokenRepository;
import com.xiaoniucode.etp.server.web.service.AccessTokenService;
import jakarta.persistence.EntityManager;
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
    @Autowired
    private AccessTokenManager accessTokenManager;
    @Autowired
    private EntityManager entityManager;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccessToken create(CreateAccessTokenRequest request) {
        String name = request.getName();
        Integer maxClient = request.getMaxClient();

        AccessToken accessToken = new AccessToken();
        accessToken.setName(name);
        accessToken.setMaxClient(maxClient);
        String token = UUID.randomUUID().toString().replaceAll("-", "");
        accessToken.setToken(token);
        accessTokenManager.addAccessToken(new AccessTokenInfo(name, token, maxClient));
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
    @Transactional(rollbackFor = Exception.class)
    public void update(UpdateAccessTokenRequest request) {
        AccessToken existingToken = accessTokenRepository.findById(request.getId())
                .orElseThrow(() -> new BizException("访问令牌不存在"));
        existingToken.setName(request.getName());
        existingToken.setMaxClient(request.getMaxClient());

        AccessTokenInfo accessToken = accessTokenManager.getAccessToken(existingToken.getToken());
        accessToken.setName(request.getName());
        accessToken.setMaxClients(request.getMaxClient());

        accessTokenRepository.save(existingToken);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer id) {
        AccessToken accessToken = accessTokenRepository.findById(id)
                .orElseThrow(() -> new BizException("访问令牌不存在"));
        accessTokenManager.removeAccessToken(accessToken.getToken());
        accessTokenRepository.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(BatchDeleteAccessTokenRequest request) {
        List<Integer> ids = request.getIds();
        List<AccessToken> accessTokens = accessTokenRepository.findByIdIn(ids);
        if (accessTokens != null && !accessTokens.isEmpty()) {
            accessTokens.forEach(accessToken -> {
                accessTokenManager.removeAccessToken(accessToken.getToken());
            });
            List<Integer> list = accessTokens.stream().map(AccessToken::getId).toList();
            entityManager.flush();
            accessTokenRepository.deleteAllByIdInBatch(list);
        }
    }
}
