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

package com.xiaoniucode.etp.server.service.his;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.server.vhost.DomainBinding;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class RegisterResult {
    private ProxyConfig proxyConfig;
    private List<DomainBinding> domainBindings;
    private Integer listenPort;

    public RegisterResult(ProxyConfig proxyConfig, List<DomainBinding> domainBindings) {
        this.proxyConfig = proxyConfig;
        this.domainBindings = domainBindings;
    }
    public RegisterResult(ProxyConfig proxyConfig, Integer listenPort) {
        this.proxyConfig = proxyConfig;
        this.listenPort = listenPort;
    }
    public static RegisterResult of(ProxyConfig proxyConfig, Integer listenPort) {
        return new RegisterResult(proxyConfig, listenPort);
    }

    public static RegisterResult of(ProxyConfig proxyConfig, List<DomainBinding> domainBindings) {
        return new RegisterResult(proxyConfig, domainBindings);
    }
}
