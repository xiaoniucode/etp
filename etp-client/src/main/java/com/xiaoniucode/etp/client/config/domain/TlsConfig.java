package com.xiaoniucode.etp.client.config.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TlsConfig {
    private Boolean enable = false;
    private String certPath;
    private String storePassPath;
}
