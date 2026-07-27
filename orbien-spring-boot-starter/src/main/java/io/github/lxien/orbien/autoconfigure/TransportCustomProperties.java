package io.github.lxien.orbien.autoconfigure;

import io.github.lxien.orbien.core.enums.TransportProtocol;
import lombok.Data;


@Data
public class TransportCustomProperties {

    /**
     * 数据传输协议
     */
    private TransportProtocol protocol;

    /**
     * 数据通道加密
     */
    private boolean encrypt = true;

    /**
     * 多路复用
     */
    private boolean multiplex = true;

    /**
     * 是否压缩
     */
    private boolean compress;

    /**
     * 压缩算法
     */
    private String compressAlgorithm = "snappy";
}
