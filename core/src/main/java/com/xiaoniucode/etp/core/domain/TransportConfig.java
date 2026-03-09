package com.xiaoniucode.etp.core.domain;

import com.xiaoniucode.etp.core.enums.TransportProtocol;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransportConfig {
    /**
     * true: 共享隧道 false：独立隧道
     */
    private boolean mux;
    private TransportProtocol protocol = TransportProtocol.TCP;
    private EncryptionConfig encrypt;
    private CompressionConfig compress;
    public static TransportConfig DEFAULT_CONFIG = new TransportConfig();
}
