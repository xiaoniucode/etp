package cn.xilio.etp.server.store.dto;

import java.io.Serializable;

public record ProxyDTO(String name, String protocol, Integer localPort, Integer remotePort) implements Serializable {
}
