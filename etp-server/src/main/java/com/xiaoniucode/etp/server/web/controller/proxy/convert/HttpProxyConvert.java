package com.xiaoniucode.etp.server.web.controller.proxy.convert;

import com.xiaoniucode.etp.server.web.controller.proxy.response.DomainWithBaseDomain;
import com.xiaoniucode.etp.server.web.controller.proxy.response.HttpProxyDTO;
import com.xiaoniucode.etp.server.web.domain.Proxy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface HttpProxyConvert {

    HttpProxyConvert INSTANCE = Mappers.getMapper(HttpProxyConvert.class);

    @Mapping(target = "protocol", expression = "java(proxy.getProtocol().getCode())")
    @Mapping(target = "domainType", expression = "java(proxy.getDomainType().getCode())")
    @Mapping(target = "clientType", expression = "java(proxy.getClientType().getCode())")
    @Mapping(target = "status", expression = "java(proxy.getStatus().getCode())")
    HttpProxyDTO toDTO(Proxy proxy, List<DomainWithBaseDomain> domains, int httpProxyPort);

    List<HttpProxyDTO> toDTOList(List<Proxy> proxies);
}
