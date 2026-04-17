package com.xiaoniucode.etp.server.web.support.store.converter;

import com.xiaoniucode.etp.server.config.domain.TokenConfig;
import com.xiaoniucode.etp.server.web.entity.AccessTokenDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TokenStoreConvert {

    TokenConfig toTokenConfig(AccessTokenDO accessTokenDO);

    List<TokenConfig> toTokenConfigList(List<AccessTokenDO> accessTokenDOs);
}
