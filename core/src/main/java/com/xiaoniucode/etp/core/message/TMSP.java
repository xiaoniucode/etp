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
    public static final byte MSG_AUTH = 0x01;  // 认证请求
    public static final byte MSG_AUTH_RESP = 0x02;  // 认证响应

    public static final byte MSG_PING = 0x03;  // 心跳请求
    public static final byte MSG_PONG = 0x04;  // 心跳响应

    public static final byte MSG_GOAWAY = 0x05;  // 优雅关闭连接
    public static final byte MSG_ERROR = 0x06;  // 通用错误消息

    // ────────────────────────────────────────────────
    // 配置管理消息（Proxy Config Control）—— 认证后才能使用
    // ────────────────────────────────────────────────
    public static final byte MSG_PROXY_CREATE = 0x10;  // 创建代理隧道（请求）
    public static final byte MSG_PROXY_CREATE_RESP = 0x11;  // 创建响应

    public static final byte MSG_PROXY_UPDATE = 0x12;  // 更新代理配置（请求）
    public static final byte MSG_PROXY_UPDATE_RESP = 0x13;  // 更新响应

    public static final byte MSG_PROXY_DELETE = 0x14;  // 删除代理隧道（请求）
    public static final byte MSG_PROXY_DELETE_RESP = 0x15;  // 删除响应

    public static final byte MSG_PROXY_LIST = 0x16;  // 查询所有代理列表（请求）
    public static final byte MSG_PROXY_LIST_RESP = 0x17;  // 返回代理列表

    public static final byte MSG_PROXY_GET = 0x18;  // 查询单个代理详情（请求）
    public static final byte MSG_PROXY_GET_RESP = 0x19;  // 返回单个代理详情

    public static final byte MSG_PROXY_NOTIFY = 0x1A;  // 服务端推送配置变更/状态变更

    // ────────────────────────────────────────────────
    // 流控制与数据消息（Stream Level）
    // ────────────────────────────────────────────────
    public static final byte MSG_STREAM_OPEN = 0x20;  // 打开新流
    public static final byte MSG_STREAM_OPEN_RESP = 0x21;  // 打开流响应

    public static final byte MSG_STREAM_CLOSE = 0x22;  // 优雅关闭流
    public static final byte MSG_STREAM_RESET = 0x23;  // 强制重置流（异常情况）

    public static final byte MSG_STREAM_DATA = 0x24;  // 实际隧道数据


    // flags 位掩码
    public static final byte FLAG_COMPRESSED = 0x01;  // 0000 0001 加密
    public static final byte FLAG_ENCRYPTED = 0x02;   // 0000 0010 压缩
    public static final byte FLAG_MUX = 0x04;        //  0000 0100 多路复用

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
