package com.xiaoniucode.etp.server.web.controller.proxy;

import com.xiaoniucode.etp.server.web.common.Ajax;
import com.xiaoniucode.etp.server.web.controller.proxy.request.HttpProxyCreateRequest;
import com.xiaoniucode.etp.server.web.controller.proxy.request.HttpProxyUpdateRequest;
import com.xiaoniucode.etp.server.web.controller.proxy.request.TcpProxyCreateRequest;
import com.xiaoniucode.etp.server.web.controller.proxy.request.TcpProxyUpdateRequest;
import com.xiaoniucode.etp.server.web.controller.proxy.response.HttpProxyDTO;
import com.xiaoniucode.etp.server.web.controller.proxy.response.TcpProxyDTO;
import com.xiaoniucode.etp.server.web.domain.Proxy;
import com.xiaoniucode.etp.server.web.service.ProxyService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 代理配置接口控制器
 *
 * @author liuxin
 */
@RestController
@RequestMapping("/api/proxies")
public class ProxyController {
    private final ProxyService proxyService;

    public ProxyController(ProxyService proxyService) {
        this.proxyService = proxyService;
    }

    /**
     * 创建 TCP 代理
     */
    @PostMapping("tcp")
    public Ajax createTcpProxy(@RequestBody TcpProxyCreateRequest request) {
        proxyService.createTcpProxy(request);
        return Ajax.success();
    }

    /**
     * 创建 HTTP 代理
     */
    @PostMapping("http")
    public Ajax createHttpProxy(@RequestBody HttpProxyCreateRequest request) {
        proxyService.createHttpProxy(request);
        return Ajax.success();
    }

    /**
     * 更新 TCP 代理
     */
    @PutMapping("tcp/{id}")
    public Ajax updateTcpProxy(@RequestBody TcpProxyUpdateRequest request) {
        proxyService.updateTcpProxy(request);
        return Ajax.success();
    }

    /**
     * 更新 HTTP 代理
     */
    @PutMapping("http/{id}")
    public Ajax updateHttpProxy(@RequestBody HttpProxyUpdateRequest request) {
        proxyService.updateHttpProxy(request);
        return Ajax.success();
    }

    /**
     * 删除代理
     */
    @DeleteMapping("{id}")
    public Ajax deleteProxy(@PathVariable Integer id) {
        proxyService.deleteProxy(id);
        return Ajax.success();
    }

    /**
     * 根据 ID 查询代理
     */
    @GetMapping("{id}/tcp")
    public Ajax getTcpProxyById(@PathVariable Integer id) {
        TcpProxyDTO proxy = proxyService.getTcpProxyById(id);
        return Ajax.success(proxy);

    }

    /**
     * 根据 ID 查询代理
     */
    @GetMapping("{id}/http")
    public Ajax getHttpProxyById(@PathVariable Integer id) {
        HttpProxyDTO proxy = proxyService.getHttpProxyById(id);
        return Ajax.success(proxy);

    }

    /**
     * 获取所有 TCP 代理
     */
    @GetMapping("tcp")
    public Ajax getTcpProxies() {
        List<TcpProxyDTO> proxies = proxyService.getTcpProxies();
        return Ajax.success(proxies);
    }

    /**
     * 获取所有 HTTP 代理
     */
    @GetMapping("http")
    public Ajax getHttpProxies() {
        List<HttpProxyDTO> proxies = proxyService.getHttpProxies();
        return Ajax.success(proxies);
    }

    /**
     * 批量删除代理
     */
    @DeleteMapping
    public Ajax batchDeleteProxies(List<Integer> ids) {
        proxyService.batchDeleteProxies(ids);
        return Ajax.success();
    }

    /**
     * 切换代理状态
     */
    @PutMapping("{id}/status")
    public Ajax switchProxyStatus(@PathVariable Integer id) {
        proxyService.switchProxyStatus(id);
        return Ajax.success();
    }

}
