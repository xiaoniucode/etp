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
package com.xiaoniucode.etp.server.web.param.proxy;

import com.xiaoniucode.etp.server.web.param.bandwidth.BandwidthSaveParam;
import com.xiaoniucode.etp.server.web.param.loadbalance.LoadBalanceParam;
import com.xiaoniucode.etp.server.web.param.proxytarget.ProxyTargetAddParam;
import com.xiaoniucode.etp.server.web.param.transport.TransportSaveParam;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TcpProxyUpdateParam {
    @NotEmpty(message = "id 不能为空")
    private String id;
    @NotEmpty(message = "name 不能为空")
    private String name;
    @NotNull(message = "status 不能为空")
    private Integer status;
    @NotNull(message = "部署模式不能为空")
    private Integer deploymentMode;
    @NotNull(message = "targets 不能为空")
    private List<ProxyTargetAddParam> targets;
    private BandwidthSaveParam bandwidth;
    private LoadBalanceParam loadBalance;
    @NotNull(message = "transport 不能为空")
    private TransportSaveParam transport;
    @NotNull(message = "remotePort 不能为空")
    private Integer remotePort;
}