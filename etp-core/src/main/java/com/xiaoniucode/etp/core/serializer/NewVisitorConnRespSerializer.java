package com.xiaoniucode.etp.core.serializer;

import com.xiaoniucode.etp.core.msg.Message;
import com.xiaoniucode.etp.core.msg.NewVisitorConnResp;
import io.netty.buffer.ByteBuf;

public class NewVisitorConnRespSerializer implements MessageSerializer <NewVisitorConnResp>{
    @Override
    public byte getMessageType() {
        return Message.TYPE_NEW_VISITOR_CONN_RESP;
    }

    @Override
    public void serialize(NewVisitorConnResp message, ByteBuf out) {

    }

    @Override
    public NewVisitorConnResp deserialize(ByteBuf in) {
        return null;
    }
}
