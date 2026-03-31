package com.xiaoniucode.etp.client.listener;

import com.xiaoniucode.etp.client.config.ConfigUtils;
import com.xiaoniucode.etp.client.event.ApplicationInitEvent;
import com.xiaoniucode.etp.core.notify.EventListener;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public class ApplicationInitListener implements EventListener<ApplicationInitEvent> {
    private final InternalLogger logger = InternalLoggerFactory.getInstance(ApplicationInitListener.class);

    @Override
    public void onEvent(ApplicationInitEvent event) {
        ConfigUtils.setConfig(event.getAppConfig());
    }
}
