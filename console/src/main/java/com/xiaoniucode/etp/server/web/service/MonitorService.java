/*
 *    Copyright 2026 xiaoniucode
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http:
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.xiaoniucode.etp.server.web.service;
import com.xiaoniucode.etp.server.web.common.server.domain.ServerInfo;
import java.util.Map;
public interface MonitorService {
    /**
     * 获取监控概览数据
     */
    Map<String, Object> getMonitorOverview();
    /**
     * 获取服务器监控数据
     */
    ServerInfo getServerMonitorData();
    /**
     * 获取流量数据
     */
    Map<String, Object> getTrafficData();
}
