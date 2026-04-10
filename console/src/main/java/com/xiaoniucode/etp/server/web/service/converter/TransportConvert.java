/*
 *    Copyright 2026 xiaoniucode
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.xiaoniucode.etp.server.web.service.converter;

import com.xiaoniucode.etp.core.enums.TunnelType;
import com.xiaoniucode.etp.server.web.dto.transport.TransportDTO;
import com.xiaoniucode.etp.server.web.entity.TransportDO;
import com.xiaoniucode.etp.server.web.param.transport.TransportSaveParam;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", imports = {TunnelType.class})
public interface TransportConvert {
    @Mapping(target = "proxyId", source = "proxyId")
    @Mapping(target = "tunnelType", expression = "java(TunnelType.fromCode(param.getTunnelType()))")
    TransportDO toDO(TransportSaveParam param, String proxyId);
    @Mapping(target = "tunnelType", expression = "java(transportDO.getTunnelType().getCode())")
    TransportDTO toDTO(TransportDO transportDO);
}

