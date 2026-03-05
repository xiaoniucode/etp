package com.xiaoniucode.etp.client.statemachine.tunnel;

import com.alibaba.cola.statemachine.StateMachine;
import com.xiaoniucode.etp.core.statemachine.context.ProcessContextImpl;
import io.netty.channel.Channel;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TunnelContext extends ProcessContextImpl {
    /**
     * 隧道唯一标识
     */
    private String tunnelId;
    /**
     * 控制隧道的连接 ID
     */
    private int connectionId;
    /**
     * 时候是多路复用
     */
    private boolean mux;
    private boolean encrypt;
    private boolean compress;
    /**
     * 连接管道
     */
    private Channel tunnel;
    private TunnelState state = TunnelState.IDLE;
    private StateMachine<TunnelState, TunnelEvent, TunnelContext> stateMachine;

    public void fireEvent(TunnelEvent event) {
        stateMachine.fireEvent(state, event, this);
    }
}
