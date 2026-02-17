package com.xiaoniucode.etp.client.handler.utils;

import com.xiaoniucode.etp.client.common.utils.OSUtils;
import com.xiaoniucode.etp.core.message.Message;

public class MessageUtils {
    public static Message.ControlMessage buildVisitorConn(String sessionId) {
        Message.MessageHeader header = Message.MessageHeader.newBuilder().setType(Message.MessageType.NEW_VISITOR_RESP).build();
        Message.NewVisitorConnResp newvisitorConnResp = Message.NewVisitorConnResp.newBuilder()
                .setSessionId(sessionId).build();
        return Message.ControlMessage.newBuilder()
                .setHeader(header)
                .setNewVisitorConnResp(newvisitorConnResp)
                .build();

    }

    public static Message.ControlMessage buildCloseProxy(String sessionId) {
        Message.MessageHeader header = Message.MessageHeader.newBuilder().setType(Message.MessageType.CLOSE_PROXY).build();
        Message.CloseProxy closeProxy = Message.CloseProxy.newBuilder().setSessionId(sessionId).build();
        return Message.ControlMessage.newBuilder()
                .setHeader(header)
                .setCloseProxy(closeProxy)
                .build();
    }

    public static Message.ControlMessage buildLogin(String clientId, String token, String version) {
        Message.MessageHeader header = Message.MessageHeader.newBuilder()
                .setType(Message.MessageType.LOGIN)
                .build();
        Message.Login login = Message.Login.newBuilder()
                .setClientId(clientId)
                .setName(OSUtils.getUsername())
                .setVersion(version)
                .setToken(token)
                .setClientType(Message.ClientType.BINARY_DEVICE)
                .setArch(OSUtils.getOSArch())
                .setOs(OSUtils.getOS()).build();
        return Message.ControlMessage.newBuilder().setHeader(header).setLogin(login).build();
    }
}
