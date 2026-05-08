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
import lombok.Getter;

import java.util.List;
import java.util.Set;

@Getter
public class ProxyReportEvent extends Event {
    /**
     * 最新的代理配置信息
     */
    private final ProxyConfig proxyConfig;
    /**
     * 基础域名，用于子域名
     */
    private String baseDomain;
    /**
     * 完整子域名列表，完整域名=子域名.基础域名
     */
    private Set<String> subdomains;
    /**
     * 是否是更新事件，true=更新事件，false=新增事件
     */
    private final boolean isUpdate;

    public ProxyReportEvent(boolean isUpdate, ProxyConfig proxyConfig) {
        this.isUpdate = isUpdate;
        this.proxyConfig = proxyConfig;
    }

    public ProxyReportEvent(boolean isUpdate, String baseDomain, Set<String> subdomains, ProxyConfig proxyConfig) {
        this.isUpdate = isUpdate;
        this.proxyConfig = proxyConfig;
        this.baseDomain = baseDomain;
        this.subdomains = subdomains;
    }
}
