package com.xiaoniucode.etp.core.domain;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransportConfig {
    private Boolean multiplex;
    private Boolean encrypt;
    private Boolean compress;
}
