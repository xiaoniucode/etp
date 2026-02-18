package com.xiaoniucode.etp.server.web.service;

import com.xiaoniucode.etp.server.web.controller.accesstoken.request.BatchDeleteAccessTokenRequest;
import com.xiaoniucode.etp.server.web.controller.accesstoken.request.CreateAccessTokenRequest;
import com.xiaoniucode.etp.server.web.controller.accesstoken.request.UpdateAccessTokenRequest;
import com.xiaoniucode.etp.server.web.controller.accesstoken.response.AccessTokenDTO;
import com.xiaoniucode.etp.server.web.entity.AccessToken;

import java.util.List;

public interface AccessTokenService {
    /**
     * 创建访问令牌
     */
    AccessToken create(CreateAccessTokenRequest request);

    /**
     * 查询所有访问令牌
     */
    List<AccessTokenDTO> findAll();

    /**
     * 根据 ID 查询访问令牌
     */
    AccessTokenDTO findById(Integer id);

    /**
     * 更新访问令牌
     */
    void update(UpdateAccessTokenRequest accessToken);

    /**
        * 删除访问令牌
        */
       void delete(Integer id);
       
       /**
        * 批量删除访问令牌
        */
       void deleteBatch(BatchDeleteAccessTokenRequest request);
}
