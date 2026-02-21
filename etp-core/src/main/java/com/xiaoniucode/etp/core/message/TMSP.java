package com.xiaoniucode.etp.core.message;

/**
 * TMSP (Tunnel Multiplexed Stream Protocol)
 * 隧道多路复用流协议
 * 二进制协议
 */
public class TMSP {
    /**
     * 协议魔数: TMSP (0x544D5350)
     */
    public static final int MAGIC = 0x544D5350;
    /** 协议版本: 1.0 */
    public static final byte VERSION = 0x10;
}
