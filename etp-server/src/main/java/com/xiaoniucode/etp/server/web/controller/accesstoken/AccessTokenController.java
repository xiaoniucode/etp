package com.xiaoniucode.etp.server.web.controller.accesstoken;

import com.xiaoniucode.etp.server.web.common.Ajax;
import com.xiaoniucode.etp.server.web.controller.accesstoken.request.CreateAccessTokenRequest;
import com.xiaoniucode.etp.server.web.controller.accesstoken.request.UpdateAccessTokenRequest;
import com.xiaoniucode.etp.server.web.controller.accesstoken.response.AccessTokenDTO;
import com.xiaoniucode.etp.server.web.domain.AccessToken;
import com.xiaoniucode.etp.server.web.service.AccessTokenService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public Ajax create(@RequestBody CreateAccessTokenRequest request) {
        AccessToken accessToken = new AccessToken();
        accessToken.setName(request.getName());
        accessToken.setMaxClient(request.getMaxClient());
        AccessToken createdToken = accessTokenService.create(accessToken);
        return Ajax.success(createdToken);
    }
    
    /**
     * 更新访问令牌
     */
    @PutMapping("/{id}")
    public Ajax update(@PathVariable Integer id, @RequestBody UpdateAccessTokenRequest request) {
        AccessToken accessToken = new AccessToken();
        accessToken.setId(id);
        accessToken.setName(request.getName());
        accessToken.setMaxClient(request.getMaxClient());
        AccessToken updatedToken = accessTokenService.update(accessToken);
        return Ajax.success(updatedToken);
    }

    /**
     * 删除访问令牌
     */
    @DeleteMapping("/{id}")
    public Ajax delete(@PathVariable Integer id) {
        accessTokenService.delete(id);
        return Ajax.success();
    }
}
