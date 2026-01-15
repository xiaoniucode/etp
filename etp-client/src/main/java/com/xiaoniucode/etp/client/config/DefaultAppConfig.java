package com.xiaoniucode.etp.client.config;

import com.xiaoniucode.etp.common.LogConfig;

public class DefaultAppConfig implements AppConfig {
    private final String serverAddr;
    private final int serverPort;
    private final String secretKey;
    private final boolean tls;
    private final TruststoreConfig truststore;
    private final int initialDelaySec;
    private final int maxRetries;
    private final int maxDelaySec;
    private final LogConfig logConfig;

    private DefaultAppConfig(Builder builder) {
        this.serverAddr = builder.serverAddr;
        this.serverPort = builder.serverPort;
        this.secretKey = builder.secretKey;
        this.tls = builder.tls;
        this.truststore = builder.truststore;
        this.initialDelaySec = builder.initialDelaySec;
        this.maxRetries = builder.maxRetries;
        this.maxDelaySec = builder.maxDelaySec;
        this.logConfig = builder.logConfig;
    }

    public static class Builder {
        private String serverAddr = "127.0.0.1";
        private int serverPort = 9527;
        private String secretKey;
        private boolean tls = false;
        private TruststoreConfig truststore;
        private int initialDelaySec = 2;
        private int maxRetries = 5;
        private int maxDelaySec = 8;
        private LogConfig logConfig;

        public Builder serverAddr(String serverAddr) {
            this.serverAddr = serverAddr;
            return this;
        }

        public Builder serverPort(int serverPort) {
            this.serverPort = serverPort;
            return this;
        }

        public Builder secretKey(String secretKey) {
            this.secretKey = secretKey;
            return this;
        }

        public Builder tls(boolean tls) {
            this.tls = tls;
            return this;
        }

        public Builder truststore(TruststoreConfig truststore) {
            this.truststore = truststore;
            return this;
        }

        public Builder initialDelaySec(int initialDelaySec) {
            this.initialDelaySec = initialDelaySec;
            return this;
        }

        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public Builder maxDelaySec(int maxDelaySec) {
            this.maxDelaySec = maxDelaySec;
            return this;
        }

        public Builder logConfig(LogConfig logConfig) {
            this.logConfig = logConfig;
            return this;
        }

        public AppConfig build() {
            if (secretKey == null || secretKey.trim().isEmpty()) {
                throw new IllegalArgumentException("secretKey must not be empty");
            }
            return new DefaultAppConfig(this);
        }
    }

    @Override
    public String getServerAddr() {
        return serverAddr;
    }

    @Override
    public int getServerPort() {
        return serverPort;
    }

    @Override
    public String getSecretKey() {
        return secretKey;
    }

    @Override
    public boolean isTls() {
        return tls;
    }

    @Override
    public TruststoreConfig getTruststore() {
        return truststore;
    }

    @Override
    public int getInitialDelaySec() {
        return initialDelaySec;
    }

    @Override
    public int getMaxRetries() {
        return maxRetries;
    }

    @Override
    public int getMaxDelaySec() {
        return maxDelaySec;
    }

    public LogConfig getLogConfig() {
        return logConfig;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class DefaultTruststoreConfig implements TruststoreConfig {
        private final String path;
        private final String storePass;

        public DefaultTruststoreConfig(String path, String storePass) {
            this.path = path;
            this.storePass = storePass;
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public String getStorePass() {
            return storePass;
        }
    }
}
