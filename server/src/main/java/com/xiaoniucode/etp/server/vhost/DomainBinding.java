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

import com.xiaoniucode.etp.core.enums.DomainType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DomainBinding {
    private String proxyId;
    /**
     * 子域名/自动域名 的基础域名
     */
    private String baseDomain;
    /**
     * 如果是子域名，此处是前缀，如果域名类型，此处是完整域名
     */
    private String domain;
    /**
     * 当前域名类型
     */
    private DomainType domainType;


    public String getFullDomain() {
        if (domainType == DomainType.CUSTOM_DOMAIN) {
            return domain;
        }
        return domain + "." + baseDomain;
    }

}
