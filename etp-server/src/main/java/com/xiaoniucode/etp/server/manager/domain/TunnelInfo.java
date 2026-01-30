package com.xiaoniucode.etp.server.manager.domain;

import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TunnelInfo {
    private Channel control;
}
