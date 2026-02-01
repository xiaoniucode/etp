package com.xiaoniucode.etp.client.manager;

import io.netty.bootstrap.Bootstrap;

public final class BootstrapManager {
    private volatile static Bootstrap serverBootstrap;
    private volatile static Bootstrap tunnelBootstrap;

    public static void initBootstraps(Bootstrap control, Bootstrap real) {
        tunnelBootstrap = control;
        serverBootstrap = real;
    }

    public static Bootstrap getRealServerBootstrap() {
        return serverBootstrap;
    }

    public static Bootstrap getTunnelBootstrap() {
        return tunnelBootstrap;
    }

}
