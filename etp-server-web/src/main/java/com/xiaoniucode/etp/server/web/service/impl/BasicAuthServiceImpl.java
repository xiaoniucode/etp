package com.xiaoniucode.etp.server.web.service.impl;

import com.xiaoniucode.etp.server.web.common.BizException;
import com.xiaoniucode.etp.server.web.controller.basicauth.convert.BasicAuthConvert;
import com.xiaoniucode.etp.server.web.controller.basicauth.dto.BasicAuthDTO;
import com.xiaoniucode.etp.server.web.controller.basicauth.dto.HttpUserDTO;
import com.xiaoniucode.etp.server.web.controller.basicauth.request.AddBasicAuthRequest;
import com.xiaoniucode.etp.server.web.controller.basicauth.request.AddHttpUserRequest;
import com.xiaoniucode.etp.server.web.controller.basicauth.request.UpdateBasicAuthRequest;
import com.xiaoniucode.etp.server.web.controller.basicauth.request.UpdateHttpUserRequest;
import com.xiaoniucode.etp.server.web.entity.BasicAuth;
import com.xiaoniucode.etp.server.web.entity.HttpUser;
import com.xiaoniucode.etp.server.web.repository.BasicAuthRepository;
import com.xiaoniucode.etp.server.web.repository.HttpUserRepository;
import com.xiaoniucode.etp.server.web.service.BasicAuthService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * BasicAuth 服务实现类
 */
@Service
public class BasicAuthServiceImpl implements BasicAuthService {

    private final BasicAuthRepository basicAuthRepository;
    private final HttpUserRepository httpUserRepository;

    public BasicAuthServiceImpl(BasicAuthRepository basicAuthRepository, HttpUserRepository httpUserRepository) {
        this.basicAuthRepository = basicAuthRepository;
        this.httpUserRepository = httpUserRepository;
    }

    @Override
    public BasicAuthDTO getByProxyId(String proxyId) {
        BasicAuth basicAuth = basicAuthRepository.findById(proxyId)
                .orElseThrow(() -> new BizException("BasicAuth not found"));
        return BasicAuthConvert.INSTANCE.toBasicAuthDTO(basicAuth);
    }

    @Override
    public void addBasicAuth(AddBasicAuthRequest request) {
        boolean exists = basicAuthRepository.existsById(request.getProxyId());
        if (!exists) {
            // 使用转换器将 AddBasicAuthRequest 转换为 BasicAuth
            BasicAuth basicAuth = BasicAuthConvert.INSTANCE.toBasicAuth(request);
            basicAuthRepository.save(basicAuth);
        }
    }

    @Override
    public void update(UpdateBasicAuthRequest request) {
        BasicAuth basicAuth = basicAuthRepository.findById(request.getProxyId())
                .orElseThrow(() -> new BizException("BasicAuth not found"));
        basicAuth.setEnable(request.getEnable());
        basicAuthRepository.save(basicAuth);
    }

    @Override
    public HttpUserDTO getHttpUserById(Integer id) {
        HttpUser httpUser = httpUserRepository.findById(id)
                .orElseThrow(() -> new BizException("HttpUser not found"));
        return BasicAuthConvert.INSTANCE.toHttpUserDTO(httpUser);
    }

    @Override
    public void addUser(AddHttpUserRequest request) {
        HttpUser exist = httpUserRepository.findByUser(request.getUser());
        if (exist != null) {
            throw new BizException("用户名已经存在");
        }
        HttpUser httpUser = new HttpUser(request.getProxyId(), request.getUser(), request.getPass());
        httpUserRepository.save(httpUser);
    }

    @Override
    public void updateUser(UpdateHttpUserRequest request) {
        HttpUser httpUser = httpUserRepository.findById(request.getId())
                .orElseThrow(() -> new BizException("HttpUser not found"));
        httpUser.setUser(request.getUser());
        httpUser.setPass(request.getPass());
        httpUserRepository.save(httpUser);
    }

    @Override
    public void deleteUser(Integer id) {
        httpUserRepository.deleteById(id);
    }

    @Override
    public List<HttpUserDTO> getHttpUsersByProxyId(String proxyId) {
        List<HttpUser> httpUsers = httpUserRepository.findByProxyId(proxyId);
        return httpUsers.stream()
                .map(BasicAuthConvert.INSTANCE::toHttpUserDTO)
                .toList();
    }

    @Override
    public void addUsers(List<AddHttpUserRequest> httpUsers) {
        List<HttpUser> users = httpUsers.stream()
                .map(BasicAuthConvert.INSTANCE::toHttpUser)
                .toList();
        httpUserRepository.saveAllAndFlush(users);
    }
}
