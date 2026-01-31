package com.xiaoniucode.etp.client.handler.utils;

import com.xiaoniucode.etp.core.message.Message;
public class MessageWrapper {
    public static Message.ControlMessage buildVisitorConn(String sessionId) {
        Message.MessageHeader header = Message.MessageHeader.newBuilder()
                .setType(Message.MessageType.NEW_VISITOR_RESP)
                .build();

        Message.NewVisitorConnResp newvisitorConnResp = Message.NewVisitorConnResp.newBuilder()
                .setSessionId(sessionId).build();
        return Message.ControlMessage.newBuilder()
                .setHeader(header)
                .setNewVisitorConnResp(newvisitorConnResp)
                .build();

    }

    public static Message.ControlMessage buildCloseProxy(String sessionId) {
        Message.MessageHeader header = Message.MessageHeader.newBuilder()
                .setType(Message.MessageType.CLOSE_PROXY)
                .build();
        Message.CloseProxy closeProxy = Message.CloseProxy.newBuilder().setSessionId(sessionId).build();
        return Message.ControlMessage.newBuilder()
                .setHeader(header)
                .setCloseProxy(closeProxy)
                .build();
    }
}
