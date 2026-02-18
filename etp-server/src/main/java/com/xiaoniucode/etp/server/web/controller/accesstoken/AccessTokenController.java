package com.xiaoniucode.etp.server.web.controller.accesstoken;

import com.xiaoniucode.etp.server.web.common.Ajax;
import com.xiaoniucode.etp.server.web.controller.accesstoken.request.BatchDeleteAccessTokenRequest;
import com.xiaoniucode.etp.server.web.controller.accesstoken.request.CreateAccessTokenRequest;
import com.xiaoniucode.etp.server.web.controller.accesstoken.request.UpdateAccessTokenRequest;
import com.xiaoniucode.etp.server.web.controller.accesstoken.response.AccessTokenDTO;
import com.xiaoniucode.etp.server.web.entity.AccessToken;
import com.xiaoniucode.etp.server.web.service.AccessTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/access-token")
public class AccessTokenController {
    @Autowired
    private AccessTokenService accessTokenService;

    /**
     * 获取所有访问令牌
     */
    @GetMapping
    public Ajax getAll() {
        List<AccessTokenDTO> accessTokens = accessTokenService.findAll();
        return Ajax.success(accessTokens);
    }

    /**
     * 根据 ID 获取访问令牌
     */
    @GetMapping("/{id}")
    public Ajax getById(@PathVariable Integer id) {
        AccessTokenDTO accessToken = accessTokenService.findById(id);
        return Ajax.success(accessToken);
    }

    /**
     * 创建访问令牌
     */
    @PostMapping
    public Ajax create(@RequestBody @Validated CreateAccessTokenRequest request) {
        AccessToken createdToken = accessTokenService.create(request);
        return Ajax.success(createdToken);
    }

    /**
     * 更新访问令牌
     */
    @PutMapping
    public Ajax update(@RequestBody @Validated UpdateAccessTokenRequest request) {
        accessTokenService.update(request);
        return Ajax.success();
    }

    /**
     * 删除访问令牌
     */
    @DeleteMapping("/{id}")
    public Ajax delete(@PathVariable Integer id) {
        accessTokenService.delete(id);
        return Ajax.success();
    }

    /**
     * 批量删除访问令牌
     */
    @DeleteMapping
    public Ajax batchDelete(@RequestBody BatchDeleteAccessTokenRequest request) {
        accessTokenService.deleteBatch(request);
        return Ajax.success();
    }
}
