package com.xiaoniucode.etp.client;

import com.xiaoniucode.etp.core.msg.NewProxyResp;

public class ProxyRespHelper {
    private static NewProxyResp newProxyResp;

    public static void set(NewProxyResp resp) {
        newProxyResp = resp;
    }

    public static NewProxyResp get() {
        if (newProxyResp == null) {
            throw new NullPointerException("NewProxyResp is null");
        }
        return newProxyResp;
    }
}
