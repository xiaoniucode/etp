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

import lombok.Getter;

import java.util.List;

/**
 * 操作结果
 */
@Getter
public class ProxyOperationResult {
    private final List<String> domains;
    private final Integer listenPort;

    public ProxyOperationResult(List<String> domains, Integer listenPort) {
        this.domains = domains;
        this.listenPort = listenPort;
    }
}