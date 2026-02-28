package com.xiaoniucode.etp.core.codec;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public class NewVisitorCodec {


    public static void encode(ByteBuf buffer, String localIp, int localPort) {
        buffer.writeInt(ipToInt(localIp));

        buffer.writeShort(localPort);
    }

    public static NewVisitorInfo decode(ByteBuf buffer) {
        int ipInt = buffer.readInt();
        String localIp = intToIp(ipInt);
        int localPort = buffer.readUnsignedShort();

        return new NewVisitorInfo(localIp, localPort);
    }

    private static int ipToInt(String ip) {
        String[] parts = ip.split("\\.");
        return (Integer.parseInt(parts[0]) << 24) |
                (Integer.parseInt(parts[1]) << 16) |
                (Integer.parseInt(parts[2]) << 8) |
                Integer.parseInt(parts[3]);
    }

    private static String intToIp(int ip) {
        return String.format("%d.%d.%d.%d",
                (ip >> 24) & 0xFF,
                (ip >> 16) & 0xFF,
                (ip >> 8) & 0xFF,
                ip & 0xFF);
    }

    @AllArgsConstructor
    @Getter
    @Setter
    public static class NewVisitorInfo {
        private final String localIp;
        private final int localPort;
    }
}