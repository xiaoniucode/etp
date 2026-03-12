package com.xiaoniucode.etp.server.exceptions;

import lombok.Getter;
public class EtpException extends RuntimeException {
    @Getter
    private final String message;

    public EtpException(String message) {
        super(message);
        this.message = message;
    }
}