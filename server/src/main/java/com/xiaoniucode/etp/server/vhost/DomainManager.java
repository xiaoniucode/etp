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

package com.xiaoniucode.etp.server.vhost;

import com.xiaoniucode.etp.core.domain.RouteConfig;
import com.xiaoniucode.etp.core.enums.DomainType;
import com.xiaoniucode.etp.server.exceptions.EtpException;

import java.util.List;
import java.util.Optional;

public interface DomainManager {

    List<DomainBinding> register(String proxyId, RouteConfig routeConfig) throws EtpException;

    DomainBinding register(String proxyId, String domain, DomainType domainType) throws EtpException;

    void unregister(String proxyId, String domain);

    void unregister(String proxyId, List<String> domains);

    void unregister(String proxyId);

    List<DomainBinding> getBoundDomains(String proxyId);

    Optional<DomainBinding> getDomainBinding(String domain);

    DomainBinding match(String domain);

    boolean exist(String domain);
}
