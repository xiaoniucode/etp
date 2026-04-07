package com.xiaoniucode.etp.server.web.controller.proxy.convert;

import com.xiaoniucode.etp.server.web.controller.proxy.response.HttpProxyDTO;
import com.xiaoniucode.etp.server.web.entity.Proxy;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface HttpProxyConvert {

    HttpProxyConvert INSTANCE = Mappers.getMapper(HttpProxyConvert.class);


    HttpProxyDTO toDTO(Proxy proxy, int httpProxyPort);

    List<HttpProxyDTO> toDTOList(List<Proxy> proxies);
}
