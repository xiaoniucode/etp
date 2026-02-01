package com.xiaoniucode.etp.client.event;

import com.xiaoniucode.etp.client.config.AppConfig;

/**
 * 应用初始化事件
 */
public class ApplicationInitEvent {
   private AppConfig appConfig;

    public ApplicationInitEvent(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    public AppConfig getAppConfig() {
        return appConfig;
    }
}
