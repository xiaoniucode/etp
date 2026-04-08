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

import com.xiaoniucode.etp.server.web.dto.proxy.HttpProxyDTO;
import com.xiaoniucode.etp.server.web.dto.proxy.TcpProxyDTO;
import com.xiaoniucode.etp.server.web.entity.ProxyDO;
import com.xiaoniucode.etp.server.web.param.proxy.HttpProxyCreateParam;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProxyConvert {
    ProxyConvert INSTANCE = Mappers.getMapper(ProxyConvert.class);

    HttpProxyDTO toHttpDTO(ProxyDO proxy, int httpProxyPort);

    List<HttpProxyDTO> toHttpDTOList(List<ProxyDO> proxies);
    ProxyDO httpToEntity(HttpProxyCreateParam request);

    TcpProxyDTO toTcpDTO(ProxyDO proxy);

    List<TcpProxyDTO> toTcpDTOList(List<ProxyDO> proxies);
}
