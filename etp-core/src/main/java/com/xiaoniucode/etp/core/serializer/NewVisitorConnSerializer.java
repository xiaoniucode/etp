package com.xiaoniucode.etp.core.serializer;

import com.xiaoniucode.etp.core.msg.Message;
import com.xiaoniucode.etp.core.msg.NewVisitorConn;
import io.netty.buffer.ByteBuf;

public class NewVisitorConnSerializer implements MessageSerializer <NewVisitorConn>{
    @Override
    public byte getMessageType() {
        return Message.TYPE_NEW_VISITOR_CONN;
    }

    @Override
    public void serialize(NewVisitorConn message, ByteBuf out) {

    }

    @Override
    public NewVisitorConn deserialize(ByteBuf in) {
        return null;
    }
}
