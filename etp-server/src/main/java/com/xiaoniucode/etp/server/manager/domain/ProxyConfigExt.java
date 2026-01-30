package com.xiaoniucode.etp.server.manager.domain;

import com.xiaoniucode.etp.server.config.domain.ProxyConfig;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class ProxyConfigExt extends ProxyConfig {
    /**
     * 用于Http(s)协议，表示最终计算出的域名，原因在于有多种生成域名的方式，如自定义域名、自定义子域名、自动生成域名
     */
    private Set<String> domains;
}
