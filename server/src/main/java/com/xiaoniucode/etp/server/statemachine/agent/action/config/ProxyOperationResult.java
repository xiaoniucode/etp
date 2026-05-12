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

package com.xiaoniucode.etp.server.statemachine.agent.action.config;

import com.xiaoniucode.etp.server.vhost.DomainInfo;
import lombok.Getter;

import java.util.Set;

/**
 * 操作结果
 */
@Getter
public class ProxyOperationResult {
    private final Set<DomainInfo> domains;
    private final Integer listenPort;
    private final boolean hasChange;

    public ProxyOperationResult(Set<DomainInfo> domains, Integer listenPort, boolean hasChange) {
        this.domains = domains;
        this.listenPort = listenPort;
        this.hasChange = hasChange;
    }
}