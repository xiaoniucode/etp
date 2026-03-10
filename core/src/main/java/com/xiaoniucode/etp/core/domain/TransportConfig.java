package com.xiaoniucode.etp.core.domain;

import com.xiaoniucode.etp.core.enums.TransportProtocol;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransportConfig {
    private boolean mux;
    private TransportProtocol protocol = TransportProtocol.TCP;
    private EncryptionConfig encrypt;
    private CompressionConfig compress;
}
