package com.xiaoniucode.etp.server.web.support.store.converter;

import com.xiaoniucode.etp.server.vhost.DomainBinding;
import com.xiaoniucode.etp.server.web.entity.HttpProxyDomainDO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DomainStoreConvert {

    DomainBinding toDomainBinding(HttpProxyDomainDO httpProxyDomainDO);

    List<DomainBinding> toDomainBindingList(List<HttpProxyDomainDO> httpProxyDomainDOs);
}
