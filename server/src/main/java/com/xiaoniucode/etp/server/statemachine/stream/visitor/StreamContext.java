package com.xiaoniucode.etp.server.statemachine.stream.visitor;

import com.alibaba.cola.statemachine.StateMachine;
import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.domain.Target;
import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import com.xiaoniucode.etp.core.statemachine.context.ProcessContextImpl;
import com.xiaoniucode.etp.server.loadbalance.LoadBalancer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@Setter
public class StreamContext extends ProcessContextImpl {
    private final Logger logger = LoggerFactory.getLogger(StreamContext.class);
    private int streamId;
    private Channel control;
    private Channel tunnel;
    private Channel visitor;
    private ProxyConfig proxyConfig;
    private String sourceAddress;
    private LoadBalancer loadBalancer;
    private Target currentTarget;
    private ClientStreamState state = ClientStreamState.INITIALIZED;
    private StateMachine<ClientStreamState, ClientStreamEvent, StreamContext> stateMachine;


    public StreamContext(int streamId, StateMachine<ClientStreamState, ClientStreamEvent, StreamContext> streamStateMachine) {
        this.streamId = streamId;
        this.stateMachine = streamStateMachine;
    }

    public void fireEvent(ClientStreamEvent event) {
        stateMachine.fireEvent(state, event, this);
    }

    public void sendPayloadToVisitor(ByteBuf payload) {
        if (payload == null || payload.refCnt() <= 0) {
            return;
        }
        if (visitor == null || !visitor.isActive()) {
            logger.debug("通道未激活，数据转发失败：streamId={}",streamId);
            payload.release();
        }
        visitor.writeAndFlush(payload).addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                visitor.close();
                logger.debug("数据转发失败，目标服务：host={},port={}", currentTarget.getHost(), currentTarget.getPort());
            }else {
                logger.debug("数据转发成功：streamId={}",streamId);
            }
        });
    }

    public void relayToTunnel(ByteBuf payload) {
        TMSPFrame frame = new TMSPFrame(streamId, TMSP.MSG_STREAM_DATA, payload);
        tunnel.writeAndFlush(frame).addListener(future -> {
           if (!future.isSuccess()){
               visitor.close();
               logger.error("数据转发失败");
           }
           if (future.isSuccess()){
               logger.debug("数据转发成功：streamId={}",streamId);
           }
        });
    }
}