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

package com.xiaoniucode.etp.server.event;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.notify.Event;
import com.xiaoniucode.etp.server.statemachine.agent.AgentInfo;
import com.xiaoniucode.etp.server.vhost.DomainInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
public class ProxyCreateEvent extends Event {
    private AgentInfo agentInfo;
    private ProxyConfig proxyConfig;
    private List<DomainInfo> subdomains;

    public ProxyCreateEvent(AgentInfo agentInfo, ProxyConfig proxyConfig) {
        this.agentInfo = agentInfo;
        this.proxyConfig = proxyConfig;
    }

    public ProxyCreateEvent(AgentInfo agentInfo, List<DomainInfo> subdomains, ProxyConfig proxyConfig) {
        this.agentInfo = agentInfo;
        this.subdomains = subdomains;
        this.proxyConfig = proxyConfig;
    }
}
