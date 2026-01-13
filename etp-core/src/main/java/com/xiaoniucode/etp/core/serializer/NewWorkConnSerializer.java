package com.xiaoniucode.etp.core.serializer;

import com.xiaoniucode.etp.core.msg.Message;
import com.xiaoniucode.etp.core.msg.NewWorkConn;
import io.netty.buffer.ByteBuf;

public class NewWorkConnSerializer implements MessageSerializer<NewWorkConn> {
    @Override
    public byte getMessageType() {
        return Message.TYPE_NEW_WORK_CONN;
    }

    @Override
    public void serialize(NewWorkConn message, ByteBuf out) {

    }

    @Override
    public NewWorkConn deserialize(ByteBuf in) {
        return null;
    }
}
