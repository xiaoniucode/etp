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

import com.xiaoniucode.etp.core.enums.DomainType;
import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.server.web.dto.proxy.HttpProxyDetailDTO;
import com.xiaoniucode.etp.server.web.dto.proxy.HttpProxyListDTO;
import com.xiaoniucode.etp.server.web.dto.proxy.TcpProxyDetailDTO;
import com.xiaoniucode.etp.server.web.dto.proxy.TcpProxyListDTO;
import com.xiaoniucode.etp.server.web.entity.ProxyDO;
import com.xiaoniucode.etp.server.web.param.proxy.HttpProxyCreateParam;
import com.xiaoniucode.etp.server.web.param.proxy.HttpProxyUpdateParam;
import com.xiaoniucode.etp.server.web.param.proxy.TcpProxyCreateParam;
import com.xiaoniucode.etp.server.web.param.proxy.TcpProxyUpdateParam;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", imports = {ProtocolType.class, DomainType.class})
public interface ProxyConvert {
    @Mapping(target = "protocol", expression = "java(ProtocolType.HTTP)")
    @Mapping(source = "proxyId", target = "id")
    ProxyDO toDO(HttpProxyCreateParam request, String proxyId);

    @Mapping(target = "protocol", expression = "java(ProtocolType.TCP)")
    @Mapping(source = "proxyId", target = "id")
    ProxyDO toDO(TcpProxyCreateParam request, String proxyId);

    @Mapping(target = "id", ignore = true)
    void updateDO(HttpProxyUpdateParam param, @MappingTarget ProxyDO proxyDO);

    @Mapping(target = "id", ignore = true)
    void updateDO(TcpProxyUpdateParam param, @MappingTarget ProxyDO proxyDO);

    @Mapping(source = "agentType", target = "agentType")
    @Mapping(source = "proxyDO.domainType", target = "domainType", qualifiedByName = "domainTypeToCode")
    HttpProxyDetailDTO toHttpDetailDTO(ProxyDO proxyDO, Integer agentType);

    @Mapping(source = "agentType", target = "agentType")
    TcpProxyDetailDTO toTcpDetailDTO(ProxyDO proxyDO, Integer agentType);

    List<HttpProxyListDTO> toHttpDTOList(List<ProxyDO> proxies);

    TcpProxyListDTO toTcpListDTO(ProxyDO proxy);

    List<TcpProxyListDTO> toTcpDTOList(List<ProxyDO> proxies);

    @Named("domainTypeToCode")
    static Integer domainTypeToCode(DomainType domainType) {
        return domainType != null ? domainType.getCode() : null;
    }

    @Mapping(source = "httpProxyPort", target = "httpProxyPort")
    HttpProxyListDTO toHttpListDTO(ProxyDO proxyDO, int httpProxyPort);
}
