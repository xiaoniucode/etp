package com.xiaoniucode.etp.autoconfigure;

import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;

/**
 * 监听 WebServer 启动事件WebServerInitializedEvent，捕获实际绑定的web端口
 *
 * @author liuxin
 */
public class WebServerPortListener {

    private volatile int actualPort;

    @EventListener
    public void onWebServerInitialized(WebServerInitializedEvent event) {
        this.actualPort = event.getWebServer().getPort();
    }

    public int getActualPort() {
        return actualPort;
    }
}
