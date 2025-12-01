package cn.xilio.etp.server.store;

public record KeystoreConfig(String path,
                             String keyPass,
                             String storePass) {
}

