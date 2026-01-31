package com.xiaoniucode.etp.client.helper;

import com.xiaoniucode.etp.core.message.Message;

public class ProxyRespHelper {
    private static Message.NewProxyResp newProxyResp;

    public static void set(Message.NewProxyResp resp) {
        newProxyResp = resp;
    }

    public static Message.NewProxyResp get() {
        return newProxyResp;
    }
}
