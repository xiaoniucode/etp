package com.xiaoniucode.etp.client.listener;

import com.xiaoniucode.etp.client.config.ConfigUtils;
import com.xiaoniucode.etp.client.event.ApplicationInitEvent;
import com.xiaoniucode.etp.client.manager.EventBusManager;
import com.xiaoniucode.etp.core.notify.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 应用首次初始化后执行
 * 1.系统配置初始化
 * 2.创建并注册系统中的其他事件监听器
 */
public class ApplicationInitListener implements EventListener<ApplicationInitEvent> {
    private final Logger logger = LoggerFactory.getLogger(ApplicationInitListener.class);

    @Override
    public void onEvent(ApplicationInitEvent event) {
        //将配置存储到配置工具类，方便全局使用
        ConfigUtils.setConfig(event.getAppConfig());

        //创建并注册系统中的事件监听器
        EventBusManager.register(new ProxyConfigSender());


    }
}
