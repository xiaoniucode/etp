package com.xiaoniucode.etp.server.web.controller.proxy.convert;

import com.xiaoniucode.etp.server.web.controller.proxy.response.TcpProxyDTO;
import com.xiaoniucode.etp.server.web.entity.Proxy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface TcpProxyConvert {

    TcpProxyConvert INSTANCE = Mappers.getMapper(TcpProxyConvert.class);

    @Mapping(target = "protocol", expression = "java(proxy.getProtocol().getCode())")
    @Mapping(target = "status", expression = "java(proxy.getStatus().getCode())")
    @Mapping(target = "clientType", expression = "java(proxy.getClientType().getCode())")
    TcpProxyDTO toDTO(Proxy proxy);

    List<TcpProxyDTO> toDTOList(List<Proxy> proxies);
}
