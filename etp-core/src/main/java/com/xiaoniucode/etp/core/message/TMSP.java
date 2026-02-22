package com.xiaoniucode.etp.core.message;

/**
 * TMSP (Tunnel Multiplexed Stream Protocol)
 * 隧道多路复用流协议
 * 传输层二进制协议
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 |                           Magic (32)                            |
 |                           (0x544D5350)                          |
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 |   Version(8)  |   MsgType(8)  |         Stream ID (32)          |
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 |                           Length (32)                           |
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 |                        Payload (可变)                        ... |
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * @author xiaoniucode
 */
public class TMSP {
    public static final String PROTOCOL_NAME = "TMSP";
    public static final int MAGIC = 0x544D5350;  // TMSP
    public static final byte VERSION = 0x10;      // 1.0

    public static final byte MSG_REQUEST = 0x01;    // 请求
    public static final byte MSG_RESPONSE = 0x02;   // 响应
    public static final byte MSG_DATA = 0x03;       // 流数据
    public static final byte MSG_PING = 0x04;       // 心跳
    public static final byte MSG_PONG = 0x05;       // 心跳响应
    public static final byte MSG_CLOSE = 0x06;      // 关闭流
    public static final byte MSG_RESET = 0x07;      // 重置流
    public static final byte MSG_GOAWAY = 0x08;     // 关闭连接
    public static final byte MSG_ERROR = 0x0F;      // 错误

    /**
     * 获取主版本号
     *
     * @param version 版本字节
     * @return 主版本号
     */
    public static int getMajorVersion(byte version) {
        return (version >> 4) & 0x0F;
    }

    /**
     * 获取次版本号
     *
     * @param version 版本字节
     * @return 次版本号
     */
    public static int getMinorVersion(byte version) {
        return version & 0x0F;
    }

    /**
     * 格式化版本号
     *
     * @param version 版本字节
     * @return 如 "1.0"
     */
    public static String formatVersion(byte version) {
        return getMajorVersion(version) + "." + getMinorVersion(version);
    }

    /**
     * 创建版本号
     *
     * @param major 主版本 (0-15)
     * @param minor 次版本 (0-15)
     * @return 版本字节
     */
    public static byte createVersion(int major, int minor) {
        return (byte) (((major & 0x0F) << 4) | (minor & 0x0F));
    }
}
