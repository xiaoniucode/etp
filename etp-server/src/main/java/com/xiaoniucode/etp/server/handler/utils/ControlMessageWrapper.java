package com.xiaoniucode.etp.server.handler.utils;

import com.xiaoniucode.etp.core.message.Message;

public class ControlMessageWrapper {

    public static Message.ControlMessage buildErrorMessage(int code, String msg) {
        Message.MessageHeader header = Message.MessageHeader.newBuilder()
                .setType(Message.MessageType.ERROR)
                .build();
        Message.Error error = Message.Error.newBuilder()
                .setCode(code)
                .setMessage(msg)
                .build();
        return Message.ControlMessage.newBuilder()
                .setHeader(header)
                .setError(error)
                .build();
    }
}
