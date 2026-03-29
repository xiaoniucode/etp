package com.xiaoniucode.etp.client.config.domain;

import lombok.Data;

@Data
public class ConnectionPoolConfig {
    private boolean enabled;

    private MultiplexPoolConfig multiplex = new MultiplexPoolConfig();
    private DirectPoolConfig direct = new DirectPoolConfig();

    @Data
    public static class MultiplexPoolConfig {
        private boolean plain;
        private boolean encrypt;
    }

    @Data
    public static class DirectPoolConfig {
        private int plainCount;
        private int encryptCount;
    }

}