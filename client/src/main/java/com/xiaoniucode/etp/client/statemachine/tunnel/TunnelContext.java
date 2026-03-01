package com.xiaoniucode.etp.client.statemachine.tunnel;

import com.alibaba.cola.statemachine.StateMachine;
import com.xiaoniucode.etp.core.statemachine.TunnelType;
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
    private int tunnelId;
    /**
     * 控制隧道的连接 ID
     */
    private int connectionId;
    /**
     * 隧道类型
     */
    private TunnelType tunnelType;
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
