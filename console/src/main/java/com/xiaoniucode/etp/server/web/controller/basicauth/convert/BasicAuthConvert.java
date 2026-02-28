package com.xiaoniucode.etp.server.web.controller.basicauth.convert;

import com.xiaoniucode.etp.server.web.controller.basicauth.dto.BasicAuthDTO;
import com.xiaoniucode.etp.server.web.controller.basicauth.dto.HttpUserDTO;
import com.xiaoniucode.etp.server.web.controller.basicauth.request.AddBasicAuthRequest;
import com.xiaoniucode.etp.server.web.controller.basicauth.request.AddHttpUserRequest;
import com.xiaoniucode.etp.server.web.entity.BasicAuth;
import com.xiaoniucode.etp.server.web.entity.HttpUser;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * BasicAuth 转换器
 */
@Mapper
public interface BasicAuthConvert {
    
    BasicAuthConvert INSTANCE = Mappers.getMapper(BasicAuthConvert.class);
    
    /**
     * 将 BasicAuth 实体转换为 BasicAuthDTO
     */
    BasicAuthDTO toBasicAuthDTO(BasicAuth basicAuth);
    
    /**
     * 将 HttpUser 实体转换为 HttpUserDTO
     */
    HttpUserDTO toHttpUserDTO(HttpUser httpUser);
    
    /**
     * 将 HttpUserDTO 转换为 HttpUser 实体
     */
    HttpUser toHttpUser(HttpUserDTO dto);
    
    /**
     * 将 AddHttpUserRequest 转换为 HttpUser 实体
     */
    HttpUser toHttpUser(AddHttpUserRequest request);
    
    /**
     * 将 AddBasicAuthRequest 转换为 BasicAuth 实体
     */
    BasicAuth toBasicAuth(AddBasicAuthRequest request);
}
