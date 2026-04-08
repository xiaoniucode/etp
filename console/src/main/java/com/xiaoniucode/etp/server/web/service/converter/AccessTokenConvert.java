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
package com.xiaoniucode.etp.server.web.service.converter;
import com.xiaoniucode.etp.server.web.dto.auth.AccessTokenDTO;
import com.xiaoniucode.etp.server.web.entity.AccessTokenDO;
import com.xiaoniucode.etp.server.web.param.accesstoken.AccessTokenCreateParam;
import com.xiaoniucode.etp.server.web.param.accesstoken.AccessTokenUpdateParam;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import java.util.List;

@Mapper
public interface AccessTokenConvert {
    AccessTokenConvert INSTANCE = Mappers.getMapper(AccessTokenConvert.class);
    
    AccessTokenDTO toDTO(AccessTokenDO entity);
    
    AccessTokenDO toEntity(AccessTokenDTO dto);
    
    List<AccessTokenDTO> toDTOList(List<AccessTokenDO> entities);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    AccessTokenDO toEntity(AccessTokenCreateParam request);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "token", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    AccessTokenDO toEntity(AccessTokenUpdateParam request);
}
