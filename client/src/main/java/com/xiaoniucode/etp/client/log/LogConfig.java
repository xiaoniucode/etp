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

package com.xiaoniucode.etp.client.log;

import ch.qos.logback.classic.Level;
import lombok.Getter;
import lombok.Setter;

/**
 * 日志配置
 *
 * @author liuxin
 */
@Getter
@Setter
public class LogConfig {
    private String path;
    private String name;
    private String archivePattern;
    private String logPattern;
    private int maxHistory;
    private String totalSizeCap;
    private Level level;

    public LogConfig(String path, String name, String archivePattern, String logPattern, int maxHistory,
                     String totalSizeCap, Level level) {
        this.path = path;
        this.name = name;
        this.archivePattern = archivePattern;
        this.logPattern = logPattern;
        this.maxHistory = maxHistory;
        this.totalSizeCap = totalSizeCap;
        this.level = level;
    }
}
