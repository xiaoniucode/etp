package com.xiaoniucode.etp.autoconfigure;

import lombok.Data;

import java.io.Serializable;

@Data
public class TransportProperties implements Serializable {
    /**
     * 是否启用多路复用隧道
     */
    private boolean multiplex = false;

    /**
     * 是否启用压缩
     */
    private boolean compress = false;

    /**
     * 是否启用加密
     */
    private boolean encrypt = false;
}
