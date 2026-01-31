package com.xiaoniucode.etp.server.manager.domain;

import com.xiaoniucode.etp.core.domain.LanInfo;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VisitorSession {
    private String sessionId;
    /**
     * 访问者连接，公网用户访问时赋值
     */
    private Channel visitor;
    /**
     * 控制隧道，用于处理各种控制指令，Agent连接共享
     */
    private Channel control;
    /**
     * 数据传输隧道，连接成功后赋值，每个visitor一个，
     */
    private Channel tunnel;
    /**
     * 目标连接服务信息
     */
    private LanInfo lanInfo;

}
