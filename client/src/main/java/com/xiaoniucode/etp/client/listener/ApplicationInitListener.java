package com.xiaoniucode.etp.client.listener;

import com.xiaoniucode.etp.client.config.ConfigUtils;
import com.xiaoniucode.etp.client.event.ApplicationInitEvent;
import com.xiaoniucode.etp.core.notify.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationInitListener implements EventListener<ApplicationInitEvent> {
    private final Logger logger = LoggerFactory.getLogger(ApplicationInitListener.class);

    @Override
    public void onEvent(ApplicationInitEvent event) {
        ConfigUtils.setConfig(event.getAppConfig());
    }
}
