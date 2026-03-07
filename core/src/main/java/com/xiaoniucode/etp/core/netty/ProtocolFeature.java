package com.xiaoniucode.etp.core.netty;

public enum ProtocolFeature {
    PLAIN, COMPRESS, ENCRYPT, ENCRYPT_COMPRESS;

    public static ProtocolFeature toProtocolFeature(boolean encrypt, boolean compress) {
        ProtocolFeature feature;
        if (encrypt && compress) {
            feature = ProtocolFeature.ENCRYPT_COMPRESS;
        } else if (encrypt) {
            feature = ProtocolFeature.ENCRYPT;
        } else if (compress) {
            feature = ProtocolFeature.COMPRESS;
        } else {
            feature = ProtocolFeature.PLAIN;
        }
        return feature;
    }
}