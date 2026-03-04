package com.xiaoniucode.etp.client.statemachine.stream;

import lombok.*;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TunnelConfig {
    private boolean isMux;
    private boolean encrypt;
    private boolean compress;
}
