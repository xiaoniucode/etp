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

import com.xiaoniucode.etp.core.enums.LoadBalanceType;
import com.xiaoniucode.etp.server.web.dto.loadbalance.LoadBalanceDTO;
import com.xiaoniucode.etp.server.web.entity.LoadBalanceDO;
import com.xiaoniucode.etp.server.web.param.loadbalance.LoadBalanceParam;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", imports = {LoadBalanceType.class})
public interface LoadBalanceConvert {
    @Mapping(target = "proxyId", source = "proxyId")
    @Mapping(target = "strategy", expression = "java(LoadBalanceType.fromCode(param.getStrategy()))")
    LoadBalanceDO toDO(LoadBalanceParam param, String proxyId);

    @Mapping(target = "strategy", expression = "java(LoadBalanceType.fromCode(param.getStrategy()))")
    void updateDO(LoadBalanceParam param, @MappingTarget LoadBalanceDO loadBalanceDO);

    @Mapping(target = "strategy", expression = "java(loadBalanceDO.getStrategy().getCode())")
    LoadBalanceDTO toDTO(LoadBalanceDO loadBalanceDO);
}

