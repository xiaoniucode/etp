package cn.xilio.etp.server.store.dto;

import java.io.Serializable;

public record ClientDTO (String name,String secretKey) implements Serializable {
}
