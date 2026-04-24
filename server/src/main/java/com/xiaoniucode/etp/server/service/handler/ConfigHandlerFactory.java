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

package com.xiaoniucode.etp.server.service.handler;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConfigHandlerFactory {
    private final List<ConfigHandler> registrars;
    
    @Autowired
    public ConfigHandlerFactory(List<ConfigHandler> registrars) {
        this.registrars = registrars;
    }
    
    public ConfigHandler getHandler(ProxyConfig config) {
        return registrars.stream()
            .filter(delegate -> delegate.supports(config))
            .findFirst()
            .orElseThrow(() -> new UnsupportedOperationException("不支持的代理类型: " + config.getClass()));
    }
}