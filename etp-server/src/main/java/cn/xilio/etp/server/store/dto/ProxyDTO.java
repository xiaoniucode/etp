package cn.xilio.etp.server.store.dto;

import java.io.Serializable;

public record ProxyDTO(String client,String secretKey,String name, String protocol, Integer localPort, Integer remotePort,Integer status) implements Serializable {
}
