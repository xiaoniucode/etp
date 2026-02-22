package com.xiaoniucode.etp.core.message;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TMSPPacket {
    /**
     * 协议魔数
     */
    private int magic = TMSP.MAGIC;

    /**
     * 协议版本
     */
    private byte version = TMSP.VERSION;
    /**
     * 流ID - 标识所属的逻辑流
     */
    private int streamId;
    /**
     * 消息类型
     *
     * @see TMSP#MSG_REQUEST
     * @see TMSP#MSG_RESPONSE
     * @see TMSP#MSG_DATA
     * @see TMSP#MSG_PING
     * @see TMSP#MSG_PONG
     * @see TMSP#MSG_CLOSE
     * @see TMSP#MSG_RESET
     * @see TMSP#MSG_GOAWAY
     * @see TMSP#MSG_ERROR
     */
    private byte msgType;

    /**
     * 数据包
     */
    private ByteBuf payload;
    /**
     * 负载长度 - payload的可读字节数
     */
    private int length;
}
