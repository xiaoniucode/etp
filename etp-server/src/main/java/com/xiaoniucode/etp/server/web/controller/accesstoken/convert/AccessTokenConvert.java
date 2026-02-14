package com.xiaoniucode.etp.server.web.controller.accesstoken.convert;

import com.xiaoniucode.etp.server.web.controller.accesstoken.response.AccessTokenDTO;
import com.xiaoniucode.etp.server.web.entity.AccessToken;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccessTokenConvert {

    @Mappings({@Mapping(target = "onlineClient", ignore = true)})
    AccessTokenDTO toDTO(AccessToken accessToken);

    List<AccessTokenDTO> toDTOList(List<AccessToken> accessTokens);
}
