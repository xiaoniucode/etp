package com.xiaoniucode.etp.autoconfigure;

import com.xiaoniucode.etp.core.enums.ProtocolType;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProxyProperties implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 公网端口
     */
    private Integer remotePort;

    /**
     * 内网 IP
     */
    private String localIp = "127.0.0.1";

    /**
     * 协议
     */
    private ProtocolType protocol = ProtocolType.HTTP;

    /**
     * 自定义域名列表
     */
    private List<String> customDomains = new ArrayList<>();

    /**
     * 是否自动生成域名，默认自动生成子域名
     */
    private Boolean autoDomain = true;

    /**
     * 子域名列表
     */
    private List<String> subDomains = new ArrayList<>();
}
