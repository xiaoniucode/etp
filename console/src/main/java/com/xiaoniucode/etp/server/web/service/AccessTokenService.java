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
import com.xiaoniucode.etp.server.web.dto.auth.AccessTokenDTO;
import com.xiaoniucode.etp.server.web.param.accesstoken.AccessTokenBatchDeleteParam;
import com.xiaoniucode.etp.server.web.param.accesstoken.AccessTokenCreateParam;
import com.xiaoniucode.etp.server.web.param.accesstoken.AccessTokenUpdateParam;
import java.util.List;
public interface AccessTokenService {
    /**
     * 创建访问令牌
     */
    AccessTokenDTO create(AccessTokenCreateParam param);
    /**
     * 查询所有访问令牌
     */
    List<AccessTokenDTO> findAll(String keyword, int page, int size);
    /**
     * 根据 ID 查询访问令牌
     */
    AccessTokenDTO findById(Integer id);
    /**
     * 更新访问令牌
     */
    void update(AccessTokenUpdateParam param);
    /**
     * 删除访问令牌
     */
    void delete(Integer id);
    /**
     * 批量删除访问令牌
     */
    void deleteBatch(AccessTokenBatchDeleteParam param);
}
