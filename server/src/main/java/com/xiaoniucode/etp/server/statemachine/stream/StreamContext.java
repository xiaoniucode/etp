package com.xiaoniucode.etp.server.statemachine.stream;

import com.alibaba.cola.statemachine.StateMachine;
import com.xiaoniucode.etp.core.netty.AttributeKeys;
import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.domain.Target;
import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import com.xiaoniucode.etp.core.statemachine.context.ProcessContextImpl;
import com.xiaoniucode.etp.server.loadbalance.LoadBalancer;
import com.xiaoniucode.etp.core.netty.NettyBatchWriteQueue;
import com.xiaoniucode.etp.server.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.server.transport.BandwidthLimiter;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.ReferenceCountUtil;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@Setter
public class StreamContext extends ProcessContextImpl {
    private final Logger logger = LoggerFactory.getLogger(StreamContext.class);
    private int streamId;
    private AgentContext agentContext;
    private Channel tunnel;
    private Channel visitor;
    private ProxyConfig proxyConfig;
    private String sourceAddress;
    private Target currentTarget;
    private StreamState state = StreamState.IDLE;
    private ProtocolType currentProtocol = ProtocolType.TCP;
    private boolean compress;
    private boolean encrypt;
    private boolean mux;
    private BandwidthLimiter bandwidthLimiter;
    private NettyBatchWriteQueue writeQueue;
    private StateMachine<StreamState, StreamEvent, StreamContext> stateMachine;


    public StreamContext(int streamId, StateMachine<StreamState, StreamEvent, StreamContext> streamStateMachine) {
        this.streamId = streamId;
        this.stateMachine = streamStateMachine;
    }

    public void fireEvent(StreamEvent event) {
        stateMachine.fireEvent(state, event, this);
    }

    public void sendPayloadToVisitor(ByteBuf payload) {
        if (payload == null || payload.refCnt() <= 0) {
            return;
        }
        if (visitor == null || !visitor.isActive()) {
            logger.debug("通道未激活，数据转发失败：streamId={}", streamId);
            payload.release();
        }
        visitor.writeAndFlush(payload).addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                visitor.close();
                logger.debug("数据转发失败，目标服务：host={},port={}", currentTarget.getHost(), currentTarget.getPort());
            } else {
                logger.debug("数据转发到隧道成功：streamId={}", streamId);
            }
        });
    }

    public void relayToTunnel(ByteBuf payload) {
        if (mux) {
            TMSPFrame frame = new TMSPFrame(streamId, TMSP.MSG_STREAM_DATA, payload);
            tunnel.writeAndFlush(frame).addListener(future -> {
                if (!future.isSuccess()) {
                    visitor.close();
                    logger.error("数据转发失败");
                } else {
                    logger.debug("数据转发到访问者成功：streamId={}", streamId);
                }
                //  ReferenceCountUtil.release(payload);
            });
//            if (writeQueue != null) {
//                writeQueue.enqueue(frame).addListener((ChannelFutureListener) future -> {
//                    if (future.isSuccess()) {
//                        logger.debug("批量发送");
//                    } else {
//                        visitor.close();
//                    }
//                });
//            } else {
//                tunnel.writeAndFlush(frame).addListener(future -> {
//                    if (!future.isSuccess()) {
//                        visitor.close();
//                        logger.error("数据转发失败");
//                    }
//                    if (future.isSuccess()) {
//                        logger.debug("数据转发成功：streamId={}", streamId);
//                    }
//                    ReferenceCountUtil.release(payload);
//                });
//            }
        }

    }

    /**
     * 发送HTTP 协议首次缓存的第一个数据包
     */
    public void relayHttpFirstPackage(boolean mux) {
        ByteBuf cached = visitor.attr(AttributeKeys.HTTP_FIRST_PACKET).get();
        if (mux && cached != null && tunnel.isWritable()) {
            TMSPFrame frame = new TMSPFrame(streamId, TMSP.MSG_STREAM_DATA, cached);
            tunnel.writeAndFlush(frame).addListener(future -> {
                if (!future.isSuccess()) {
                    logger.error("HTTP 首包转发失败");
                }
            });

        } else if (cached != null && tunnel.isWritable()) {
            tunnel.writeAndFlush(cached).addListener((ChannelFutureListener) future -> {
                if (!future.isSuccess()) {
                    logger.error("数据转发失败");
                }
            });
        }
        visitor.attr(AttributeKeys.HTTP_FIRST_PACKET).set(null);
    }
}