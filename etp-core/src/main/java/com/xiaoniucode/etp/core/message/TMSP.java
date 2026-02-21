package com.xiaoniucode.etp.core.message;

/**
 * TMSP (Tunnel Multiplexed Stream Protocol)
 * 隧道多路复用流协议
 * 二进制协议
 */
public class TMSP {
    public static final String PROTOCOL_NAME = "TMSP";
    /**
     * 协议魔数: TMSP (0x544D5350)
     * ASCII: T=0x54, M=0x4D, S=0x53, P=0x50
     */
    public static final int MAGIC = 0x544D5350;

    /**
     * 协议版本: 1.0
     * 高4位: 主版本 1
     * 低4位: 次版本 0
     */
    public static final byte VERSION = 0x10;

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
