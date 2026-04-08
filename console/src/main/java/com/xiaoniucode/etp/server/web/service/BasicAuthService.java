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
package com.xiaoniucode.etp.server.web.service;
import com.xiaoniucode.etp.server.web.dto.auth.BasicAuthDTO;
import com.xiaoniucode.etp.server.web.dto.proxyuser.HttpUserDTO;
import com.xiaoniucode.etp.server.web.param.basicauth.BasicAuthAddParam;
import com.xiaoniucode.etp.server.web.param.basicauth.BasicAuthUpdateParam;
import com.xiaoniucode.etp.server.web.param.basicauth.httpuser.HttpUserAddParam;
import com.xiaoniucode.etp.server.web.param.basicauth.httpuser.HttpUserUpdateParam;
import java.util.List;
/**
 * BasicAuth 服务接口
 */
public interface BasicAuthService {
    /**
     * 根据代理 ID 获取 BasicAuth 信息
     */
    BasicAuthDTO getByProxyId(String proxyId);
    void addBasicAuth(BasicAuthAddParam request);
    /**
     * 更新 BasicAuth 信息
     */
    void update(BasicAuthUpdateParam request);
    /**
     * 根据代理 ID 获取 HttpUser 信息
     */
    HttpUserDTO getHttpUserById(Integer id);
    /**
     * 添加 HttpUser
     */
    void addUser(HttpUserAddParam request);
    /**
     * 更新 HttpUser
     */
    void updateUser(HttpUserUpdateParam request);
    /**
     * 删除 HttpUser
     */
    void deleteUser(Integer id);
    /**
     * 获取Basic Auth 用户列表
     *
     * @param proxyId 代理ID
     * @return 用户列表
     */
    List<HttpUserDTO> getHttpUsersByProxyId(String proxyId);
}
