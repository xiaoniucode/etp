package com.xiaoniucode.etp.server.statemachine.tunnel;

import com.alibaba.cola.statemachine.StateMachine;
import com.xiaoniucode.etp.core.statemachine.context.ProcessContextImpl;
import io.netty.channel.Channel;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public  class TunnelContext extends ProcessContextImpl {
    /**
     * 隧道唯一标识
     */
    private String tunnelId;
    /**
     * 客户端连接 ID
     */
    private int connectionId;
    /**
     * 是否是多路复用隧道
     */
    private boolean isMux;
    /**
     * 连接管道
     */
    private Channel tunnel;
    /**
     * 控制隧道
     */
    private Channel control;
    /**
     * 连接状态
     */
    private TunnelState state = TunnelState.IDLE;
    private StateMachine<TunnelState, TunnelEvent, TunnelContext> stateMachine;
    public boolean isActive() {
        return tunnel != null && tunnel.isActive();
    }
    public void fireEvent(TunnelEvent event) {
        stateMachine.fireEvent(getState(), event, this);
    }
}
