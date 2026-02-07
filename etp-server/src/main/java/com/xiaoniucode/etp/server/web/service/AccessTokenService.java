package com.xiaoniucode.etp.server.web.service;

import com.xiaoniucode.etp.server.web.domain.AccessToken;

import java.util.List;

public interface AccessTokenService {
    /**
     * 创建访问令牌
     */
    AccessToken create(AccessToken accessToken);

    /**
     * 查询所有访问令牌
     */
    List<AccessToken> findAll();

    /**
     * 根据 ID 查询访问令牌
     */
    AccessToken findById(Integer id);

    /**
     * 更新访问令牌
     */
    AccessToken update(AccessToken accessToken);

    /**
     * 删除访问令牌
     */
    void delete(Integer id);
}
