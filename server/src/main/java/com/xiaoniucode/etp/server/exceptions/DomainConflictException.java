package com.xiaoniucode.etp.server.exceptions;

public class DomainConflictException extends EtpException {
    public DomainConflictException(String domain) {
        super("域名已被占用: " + domain);
    }
}
