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

import com.xiaoniucode.etp.core.enums.DeploymentMode;
import com.xiaoniucode.etp.core.enums.ProxyStatus;
import com.xiaoniucode.etp.server.web.param.bandwidth.BandwidthSaveParam;
import com.xiaoniucode.etp.server.web.param.loadbalance.LoadBalanceParam;
import com.xiaoniucode.etp.server.web.param.transport.TransportSaveParam;
import com.xiaoniucode.etp.server.web.support.validation.EnumValue;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * TCP 代理请求参数类
 */
@Getter
@Setter
public class TcpProxyCreateParam {
    @NotEmpty(message = "agentId 不能为空")
    private String agentId;
    @NotEmpty(message = "name 不能为空")
    private String name;
    @NotNull(message = "status 不能为空")
    @EnumValue(enumClass = ProxyStatus.class)
    private Integer status;
    @NotNull(message = "部署模式不能为空")
    @EnumValue(enumClass = DeploymentMode.class)
    private Integer deploymentMode;

    @NotNull(message = "目标服务不能为空")
    @Valid
    @Size(min = 1, max = 100, message = "目标服务数不在范围内：[1-100]")
    private List<ProxyTargetSaveParam> targets;
    @Valid
    private BandwidthSaveParam bandwidth;
    @Valid
    private LoadBalanceParam loadBalance;
    @Valid
    @NotNull(message = "transport 不能为空")
    private TransportSaveParam transport;

    @Min(value = 1, message = "远程端口号不能小于1")
    @Max(value = 65535, message = "远程端口号不能大于65535")
    private Integer remotePort;
}