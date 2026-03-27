package com.xiaoniucode.etp.core.message;

/**
 * TMSP (Tunnel Multiplexed Stream Protocol)
 * 多路复用流协议
 */
public class TMSP {
    public static final String PROTOCOL_NAME = "TMSP";
    public static final int MAGIC = 0x544D5350; // 'T','M','S','P'
    public static final byte VERSION = 0x10;     // 1.0

    // ────────────────────────────────────────────────
    // 连接控制消息（Connection Level）
    // ────────────────────────────────────────────────
    public static final byte MSG_AUTH = 0x01;           // 认证请求
    public static final byte MSG_AUTH_RESP = 0x02;      // 认证响应

    public static final byte MSG_PING = 0x03;           // 心跳请求
    public static final byte MSG_PONG = 0x04;           // 心跳响应

    public static final byte MSG_GOAWAY = 0x05;         // 优雅关闭连接
    public static final byte MSG_ERROR = 0x06;          // 通用错误消息
    public static final byte MSG_TUNNEL_CREATE = 0x07;  // 隧道创建 客户端发起
    public static final byte MSG_TUNNEL_CREATE_RESP = 0x08;    // 隧道创建响应 服务端响应
    public static final byte MSG_SERVICE_HEALTH_CHANGE = 0x09;    //内网服务健康改变

    // ────────────────────────────────────────────────
    // 配置管理消息（Proxy Config Control）—— 认证后才能使用
    // ────────────────────────────────────────────────
    public static final byte MSG_PROXY_CREATE = 0x10;
    public static final byte MSG_PROXY_CREATE_RESP = 0x11;

    public static final byte MSG_PROXY_UPDATE = 0x12;
    public static final byte MSG_PROXY_UPDATE_RESP = 0x13;

    public static final byte MSG_PROXY_DELETE = 0x14;
    public static final byte MSG_PROXY_DELETE_RESP = 0x15;

    public static final byte MSG_PROXY_LIST = 0x16;
    public static final byte MSG_PROXY_LIST_RESP = 0x17;

    public static final byte MSG_PROXY_GET = 0x18;
    public static final byte MSG_PROXY_GET_RESP = 0x19;

    public static final byte MSG_PROXY_NOTIFY = 0x1A;

    // ────────────────────────────────────────────────
    // 流控制与数据消息（Stream Level）
    // ────────────────────────────────────────────────
    public static final byte MSG_STREAM_OPEN = 0x20;      // 打开新流
    public static final byte MSG_STREAM_OPEN_RESP = 0x21;  // 打开流响应

    public static final byte MSG_STREAM_CLOSE = 0x22;  // 优雅关闭流
    public static final byte MSG_STREAM_RESET = 0x23;  // 强制重置流

    public static final byte MSG_STREAM_DATA = 0x24;  // 实际隧道数据

    // flags 位掩码
    public static final byte FLAG_COMPRESSED = 0x01;  // 0000 0001 加密
    public static final byte FLAG_ENCRYPTED = 0x02;   // 0000 0010 压缩
    public static final byte FLAG_MUX = 0x04;        //  0000 0100 多路复用

    // bit 3~5 用来表示压缩算法类型（最多支持 8 种）
    public static final byte COMPRESS_NONE       = 0x00;   // 0000 0000
    public static final byte COMPRESS_LZ4        = 0x08;   // 0000 1000  (bit 3)
    public static final byte COMPRESS_SNAPPY     = 0x10;   // 0001 0000  (bit 4)

    public static final byte COMPRESS_MASK       = 0x38;   // 0011 1000  (bit 3~5，用于掩码)
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
