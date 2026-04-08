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
import com.xiaoniucode.etp.server.web.dto.auth.BasicAuthDTO;
import com.xiaoniucode.etp.server.web.dto.proxyuser.HttpUserDTO;
import com.xiaoniucode.etp.server.web.entity.BasicAuthDO;
import com.xiaoniucode.etp.server.web.entity.BasicUserDO;
import com.xiaoniucode.etp.server.web.param.basicauth.httpuser.HttpUserAddParam;
import com.xiaoniucode.etp.server.web.param.basicauth.httpuser.HttpUserUpdateParam;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface BasicAuthConvert {
    BasicAuthConvert INSTANCE = Mappers.getMapper(BasicAuthConvert.class);
    
    BasicAuthDTO toBasicAuthDTO(BasicAuthDO basicAuth);
    
    HttpUserDTO toHttpUserDTO(BasicUserDO httpUser);
    
    BasicUserDO toHttpUser(HttpUserDTO dto);
    
    BasicUserDO toHttpUser(HttpUserAddParam request);
    
    BasicAuthDO toBasicAuth(HttpUserUpdateParam request);
}
