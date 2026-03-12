package com.xiaoniucode.etp.server.exceptions;

public class PortConflictException extends EtpException {
    public PortConflictException(int port) {
        super("端口已被占用: " + port);
    }
}

