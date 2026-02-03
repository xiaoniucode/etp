package com.xiaoniucode.etp.client.listener;

import com.xiaoniucode.etp.client.config.AppConfig;
import com.xiaoniucode.etp.client.config.ConfigUtils;
import com.xiaoniucode.etp.client.config.ProxyRegistrar;
import com.xiaoniucode.etp.client.event.LoginSuccessEvent;
import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.notify.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 登陆系统成功向代理服务端发送代理配置
 */
public class ProxyConfigSender implements EventListener<LoginSuccessEvent> {
    private final Logger logger = LoggerFactory.getLogger(ProxyConfigSender.class);
    private final AtomicBoolean isSend = new AtomicBoolean(false);

    @Override
    public void onEvent(LoginSuccessEvent event) {
        //todo
//        if (isSend.get()) {
//            return;
//        }
        logger.debug("发送代理配置规则信息");
        AppConfig config = ConfigUtils.getConfig();
        List<ProxyConfig> proxies = config.getProxies();
        ProxyRegistrar.registerBatch(proxies);
//        isSend.set(true);
    }
}
