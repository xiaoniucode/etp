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

package com.xiaoniucode.etp.server.web.support.store.converter;

import com.xiaoniucode.etp.core.domain.*;
import com.xiaoniucode.etp.core.enums.TunnelType;
import com.xiaoniucode.etp.server.web.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface ProxyStoreConvert {
    @Mapping(target = "proxyId",source = "id")
    ProxyConfig toBaseDomain(ProxyDO proxyDO);

    List<Target> toTargetDomains(List<ProxyTargetDO> proxyTargetDos);

    LoadBalanceConfig toLoadBalanceDomain(LoadBalanceDO loadBalanceDO);

    default BandwidthConfig toBandwidthDomain(BandwidthDO bandwidthDO) {
        return new BandwidthConfig(bandwidthDO.getLimitTotal(), bandwidthDO.getLimitIn(), bandwidthDO.getLimitOut());
    }

    default TransportCustomConfig toTransportDomain(TransportDO transportDO) {
        TransportCustomConfig tc = new TransportCustomConfig();
        tc.setMultiplex(transportDO.getTunnelType() == TunnelType.MULTIPLEX);
        tc.setEncrypt(transportDO.getEncrypt());
        return tc;
    }

    AccessControlConfig toAccessControlDomain(AccessControlDO accessControlDO);

    BasicAuthConfig toBasicAuthDomain(BasicAuthDO basicAuthDO);

    Set<HttpUser> toBasicUserDomains(List<BasicUserDO> basicUsers);
}
