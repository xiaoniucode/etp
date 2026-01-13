package com.xiaoniucode.etp.core.serializer;

import com.xiaoniucode.etp.core.msg.KickoutClient;
import com.xiaoniucode.etp.core.msg.Message;
import io.netty.buffer.ByteBuf;

public class KickoutClientSerializer implements MessageSerializer<KickoutClient> {
    @Override
    public byte getMessageType() {
        return Message.TYPE_KICKOUT_CLIENT;
    }

    @Override
    public void serialize(KickoutClient message, ByteBuf out) {

    }

    @Override
    public KickoutClient deserialize(ByteBuf in) {
        return new KickoutClient();
    }
}
