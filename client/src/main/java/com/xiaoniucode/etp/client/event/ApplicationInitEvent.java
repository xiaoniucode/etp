package com.xiaoniucode.etp.client.event;

import com.xiaoniucode.etp.client.config.AppConfig;
import lombok.Getter;

/**
 * 应用初始化事件
 * @author xiaoniucode
 */
@Getter
public class ApplicationInitEvent {
   private final AppConfig appConfig;

    public ApplicationInitEvent(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

}
