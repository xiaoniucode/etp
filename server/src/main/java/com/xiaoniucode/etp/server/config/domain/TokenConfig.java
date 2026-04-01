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
package com.xiaoniucode.etp.server.config.domain;
import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class TokenConfig {
    public static final int UNLIMITED_CLIENTS = -1;
    public static final int UNLIMITED_DEVICES = -1;
    public static final int UNLIMITED_CONNECTIONS = -1;
    public static final int UNLIMITED_TIMEOUT = -1;
    private String name;
    private String token;
    private Integer maxClients;
    private Integer maxDevices;
    private Integer maxConnections;
    private Integer deviceTimeout;

    public TokenConfig(String name, String token, Integer maxClients, Integer maxDevices, Integer maxConnections, Integer deviceTimeout) {
        check(name, token, maxClients, maxDevices, maxConnections, deviceTimeout);
        this.name = name;
        this.token = token;
        this.maxClients = maxClients;
        this.maxDevices = maxDevices;
        this.maxConnections = maxConnections;
        this.deviceTimeout = deviceTimeout;
    }

    private void check(String name, String token, Integer maxClients, Integer maxDevices, Integer maxConnections, Integer deviceTimeout) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Token 名称不能为空");
        }
        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("Token 令牌不能为空");
        }
        // null 或 <= 0 都表示不限制
        if (maxClients == null || maxClients <= 0) {
            this.maxClients = UNLIMITED_CLIENTS;
        } else {
            this.maxClients = maxClients;
        }
        if (maxDevices == null || maxDevices <= 0) {
            this.maxDevices = UNLIMITED_DEVICES;
        } else {
            this.maxDevices = maxDevices;
        }
        if (maxConnections == null || maxConnections <= 0) {
            this.maxConnections = UNLIMITED_CONNECTIONS;
        } else {
            this.maxConnections = maxConnections;
        }
        if (deviceTimeout == null || deviceTimeout <= 0) {
            this.deviceTimeout = UNLIMITED_TIMEOUT;
        } else {
            this.deviceTimeout = deviceTimeout;
        }
    }
}