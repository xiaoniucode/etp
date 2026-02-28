package com.xiaoniucode.etp.server.web.service;

import com.xiaoniucode.etp.server.web.controller.basicauth.dto.BasicAuthDTO;
import com.xiaoniucode.etp.server.web.controller.basicauth.dto.HttpUserDTO;
import com.xiaoniucode.etp.server.web.controller.basicauth.request.AddBasicAuthRequest;
import com.xiaoniucode.etp.server.web.controller.basicauth.request.AddHttpUserRequest;
import com.xiaoniucode.etp.server.web.controller.basicauth.request.UpdateBasicAuthRequest;
import com.xiaoniucode.etp.server.web.controller.basicauth.request.UpdateHttpUserRequest;

import java.util.List;

/**
 * BasicAuth 服务接口
 */
public interface BasicAuthService {
    
    /**
     * 根据代理 ID 获取 BasicAuth 信息
     */
    BasicAuthDTO getByProxyId(String proxyId);
    void addBasicAuth(AddBasicAuthRequest request);
    /**
     * 更新 BasicAuth 信息
     */
    void update(UpdateBasicAuthRequest request);
    
    /**
     * 根据代理 ID 获取 HttpUser 信息
     */
    HttpUserDTO getHttpUserById(Integer id);
    
    /**
     * 添加 HttpUser
     */
    void addUser(AddHttpUserRequest request);
    
    /**
     * 更新 HttpUser
     */
    void updateUser(UpdateHttpUserRequest request);
    
    /**
     * 删除 HttpUser
     */
    void deleteUser(Integer id);

    /**
     * 获取Basic Auth 用户列表
     * @param proxyId 代理ID
     * @return 用户列表
     */
    List<HttpUserDTO> getHttpUsersByProxyId(String proxyId);

    void addUsers(List<AddHttpUserRequest> httpUsers);
}
