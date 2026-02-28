package com.xiaoniucode.etp.core.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MultiplexConfig {
    /**
     * true=共享隧道, false=独立隧道
     */
    private Boolean enable;
}
