package com.xiaoniucode.etp.core.domain;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransportConfig {
    private Boolean mux;
    private Boolean encrypt;
    private Boolean compress;
}
