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

package com.xiaoniucode.etp.client.common;

import lombok.Getter;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.io.InputStream;
import java.util.Properties;

public class AppVersionUtil {
    private AppVersionUtil() {
    }

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(AppVersionUtil.class);

    @Getter
    private static final String version;
    static {
        String ver = "unknown";
        try (InputStream is = AppVersionUtil.class.getClassLoader()
                .getResourceAsStream("version.properties")) {

            if (is == null) {
                logger.warn("version.properties 未找到");
            } else {
                Properties props = new Properties();
                props.load(is);
                ver = props.getProperty("app.version", "unknown").trim();
                if (ver.isEmpty() || "unknown".equals(ver)) {
                    logger.warn("app.version 未正确注入，请检查 pom.xml resource filtering 配置");
                }
            }
        } catch (Exception e) {
            logger.warn("读取版本信息失败", e);
        }

        version = ver;
    }
}
