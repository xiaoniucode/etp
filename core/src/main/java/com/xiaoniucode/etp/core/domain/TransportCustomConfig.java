package com.xiaoniucode.etp.core.domain;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransportCustomConfig {
    private Boolean multiplex = true;
    private Boolean encrypt = true;
    private Boolean compress = true;
}
