package com.xiaoniucode.etp.client.helper;

import com.xiaoniucode.etp.client.TunnelClient;

/**
 * 方便在其他地方调用隧道方法，如stop
 */
public class TunnelClientHelper {
    private static TunnelClient tunnelClient;

    public static void setTunnelClient(TunnelClient tunnelClient) {
        TunnelClientHelper.tunnelClient = tunnelClient;
    }

    public static TunnelClient getTunnelClient() {
        return tunnelClient;
    }
}
