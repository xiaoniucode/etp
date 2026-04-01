package com.xiaoniucode.etp.server.config.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class PortPolicyConfig {
    private int start;
    private int end;
}
