package com.xiaoniucode.etp.client.common.utils;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

public class AppVersionUtil {
    private AppVersionUtil() {
    }

    private static final Logger log = LoggerFactory.getLogger(AppVersionUtil.class);

    @Getter
    private static final String version;
    static {
        String ver = "unknown";
        try (InputStream is = AppVersionUtil.class.getClassLoader()
                .getResourceAsStream("version.properties")) {

            if (is == null) {
                log.warn("version.properties 未找到");
            } else {
                Properties props = new Properties();
                props.load(is);
                ver = props.getProperty("app.version", "unknown").trim();
                if (ver.isEmpty() || "unknown".equals(ver)) {
                    log.warn("app.version 未正确注入，请检查 pom.xml resource filtering 配置");
                }
            }
        } catch (Exception e) {
            log.warn("读取版本信息失败", e);
        }

        version = ver;
    }
}
