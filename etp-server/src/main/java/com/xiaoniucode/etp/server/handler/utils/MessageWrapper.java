package com.xiaoniucode.etp.server.handler.utils;

import com.xiaoniucode.etp.core.message.Message;

public class MessageWrapper {

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

    public static Message.ControlMessage buildPong() {
        Message.MessageHeader header = Message.MessageHeader.newBuilder()
                .setType(Message.MessageType.PONG)
                .build();
        Message.Pong body = Message.Pong.newBuilder().build();
        return Message.ControlMessage.newBuilder()
                .setHeader(header)
                .setPong(body)
                .build();
    }

    public static Message.ControlMessage buildLoginResp(String sessionId) {
        Message.MessageHeader heder = Message.MessageHeader.newBuilder()
                .setType(Message.MessageType.LOGIN_RESP)
                .build();
        Message.LoginResp loginResp = Message.LoginResp.newBuilder()
                .setSessionId(sessionId)
                .build();

        return Message.ControlMessage.newBuilder()
                .setHeader(heder)
                .setLoginResp(loginResp)
                .build();
    }

    public static Message.ControlMessage buildCloseProxy(String sessionId) {
        Message.MessageHeader header = Message.MessageHeader.newBuilder()
                .setType(Message.MessageType.CLOSE_PROXY)
                .build();

        Message.CloseProxy closeProxy = Message.CloseProxy
                .newBuilder()
                .setSessionId(sessionId)
                .build();

        return Message.ControlMessage.newBuilder()
                .setHeader(header)
                .setCloseProxy(closeProxy)
                .build();
    }

    public static Message.ControlMessage buildNewVisitorConn(String sessionId, String localIp, Integer localPort, Boolean compress, Boolean encrypt) {

        Message.MessageHeader header = Message.MessageHeader.newBuilder()
                .setType(Message.MessageType.NEW_VISITOR)
                .build();
        Message.NewVisitorConn newVisitorConn = Message.NewVisitorConn.newBuilder()
                .setSessionId(sessionId)
                .setLocalIp(localIp)
                .setLocalPort(localPort)
                .setCompress(compress)
                .setEncrypt(encrypt)
                .build();
        return Message.ControlMessage.newBuilder()
                .setHeader(header)
                .setNewVisitorConn(newVisitorConn)
                .build();
    }

    public static Message.ControlMessage buildKickout() {
        Message.MessageHeader header = Message.MessageHeader.newBuilder()
                .setType(Message.MessageType.KICKOUT)
                .build();
        Message.Kickout kickout = Message.Kickout.newBuilder().build();
        return Message.ControlMessage.newBuilder()
                .setHeader(header)
                .setKickout(kickout)
                .build();
    }

    public static Message.ControlMessage heartbeatTimeout() {
        Message.MessageHeader header = Message.MessageHeader.newBuilder()
                .setType(Message.MessageType.HEARTBEAT_TIMEOUT)
                .build();
        Message.HeartbeatTimeout heartbeatTimeout = Message.HeartbeatTimeout.newBuilder().build();
        return Message.ControlMessage.newBuilder()
                .setHeader(header)
                .setHeartbeatTimeout(heartbeatTimeout)
                .build();
    }


    public static Message.ControlMessage buildNewProxyResp(String name, String remoteAddr) {
        Message.MessageHeader header = Message.MessageHeader.newBuilder().setType(Message.MessageType.NEW_PROXY_RESP).build();
        Message.NewProxyResp body = Message.NewProxyResp.newBuilder().setProxyName(name).setRemoteAddr(remoteAddr).build();
        return Message.ControlMessage.newBuilder()
                .setHeader(header)
                .setNewProxyResp(body)
                .build();
    }
}
