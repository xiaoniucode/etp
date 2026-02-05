package com.xiaoniucode.etp.server.web.controller.proxy;

import com.xiaoniucode.etp.server.web.service.ProxyService;
import org.springframework.web.bind.annotation.*;

/**
 * 代理配置接口控制器
 *
 * @author liuxin
 */
@RestController
@RequestMapping("/proxies")
public class ProxyController {

    private final ProxyService proxyService;

    public ProxyController(ProxyService proxyService) {
        this.proxyService = proxyService;
    }

}
