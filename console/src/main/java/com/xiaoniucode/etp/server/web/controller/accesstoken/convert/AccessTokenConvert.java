package com.xiaoniucode.etp.server.web.controller.accesstoken.convert;

import com.xiaoniucode.etp.server.web.controller.accesstoken.request.CreateAccessTokenRequest;
import com.xiaoniucode.etp.server.web.controller.accesstoken.request.UpdateAccessTokenRequest;
import com.xiaoniucode.etp.server.web.controller.accesstoken.response.AccessTokenDTO;
import com.xiaoniucode.etp.server.web.entity.AccessToken;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 访问令牌转换器
 */
@Mapper
public interface AccessTokenConvert {
    AccessTokenConvert INSTANCE = Mappers.getMapper(AccessTokenConvert.class);

    /**
     * 实体转 DTO
     */
    AccessTokenDTO toDTO(AccessToken entity);

    /**
     * DTO 转实体
     */
    AccessToken toEntity(AccessTokenDTO dto);

    /**
     * 实体列表转 DTO 列表
     */
    List<AccessTokenDTO> toDTOList(List<AccessToken> entities);

    /**
     * 创建请求转实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    AccessToken toEntity(CreateAccessTokenRequest request);

    /**
     * 更新请求转实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "token", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    AccessToken toEntity(UpdateAccessTokenRequest request);
}
