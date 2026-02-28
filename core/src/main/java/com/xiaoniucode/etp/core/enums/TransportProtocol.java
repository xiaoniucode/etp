package com.xiaoniucode.etp.core.enums;

public enum TransportProtocol {
    TCP;
    public static TransportProtocol fromName(String name) {
        for (TransportProtocol protocol : TransportProtocol.values()) {
            if (protocol.name().equalsIgnoreCase(name)) {
                return protocol;
            }
        }
        return null;
    }
}
