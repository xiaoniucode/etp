package com.xiaoniucode.etp.client.config;

import com.xiaoniucode.etp.common.config.Config;
import com.xiaoniucode.etp.common.log.LogConfig;

public interface AppConfig extends Config {
    String getServerAddr();

    int getServerPort();

    String getSecretKey();

    boolean isTls();

    TruststoreConfig getTruststore();

    int getInitialDelaySec();

    int getMaxRetries();

    int getMaxDelaySec();

    LogConfig getLogConfig();

    interface TruststoreConfig {
        String getPath();

        String getStorePass();
    }
}
