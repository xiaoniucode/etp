package com.xiaoniucode.etp.server.utils;

import com.xiaoniucode.etp.core.message.Message;

public class CommandMessageUtils {

    public static Message.ConfigMessage buildErrorMessage(int code, String msg) {

        Message.Error error = Message.Error.newBuilder()
                .setCode(code)
                .setMessage(msg)
                .build();
        return Message.ConfigMessage.newBuilder()
                .setError(error)
                .build();
    }

    public static Message.ConfigMessage buildNewProxyResp(String name, String remoteAddr) {
        Message.NewProxyResp body = Message.NewProxyResp.newBuilder().setProxyName(name).setRemoteAddr(remoteAddr).build();
        return Message.ConfigMessage.newBuilder()
                .setNewProxyResp(body)
                .build();
    }
}
