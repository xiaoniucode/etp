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

package com.xiaoniucode.etp.server.web.dto.proxy.embedded;

import com.xiaoniucode.etp.server.web.dto.accesscontrol.AccessControlDetailDTO;
import com.xiaoniucode.etp.server.web.dto.agent.AgentDTO;
import com.xiaoniucode.etp.server.web.dto.basicauth.BasicAuthDetailDTO;
import com.xiaoniucode.etp.server.web.dto.proxy.TargetDTO;
import com.xiaoniucode.etp.server.web.dto.transport.TransportDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Data
public class TunnelDetailDTO implements Serializable {
    private AgentDTO agent;
    private ProxyDTO proxy;
    private Integer httpProxyPort;

    @Data
    public static class ProxyDTO {
        private String proxyId;
        private String name;
        private Integer protocol;
        private Integer status;
        private List<TargetDTO> targets;
        private Integer deploymentMode;
        private TransportDTO transport;
        private BandwidthDTO bandwidth;
        private AccessControlDetailDTO accessControl;
    }

    @Data
    public static class BandwidthDTO {
        /**
         * 总带宽限制
         */
        private String limitTotal;
        /**
         * 入站带宽限制
         */
        private String limitIn;
        /**
         * 出站带宽限制
         */
        private String limitOut;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class TcpProxyDTO extends ProxyDTO {
        private Integer listenPort;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class HttpProxyDTO extends ProxyDTO {
        private Set<String> domains;
        private Integer domainType;
        private BasicAuthDetailDTO basicAuth;
    }

}
