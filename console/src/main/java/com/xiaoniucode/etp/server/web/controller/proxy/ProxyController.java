//package com.xiaoniucode.etp.server.web.controller.proxy;
//
//import com.xiaoniucode.etp.server.web.common.Ajax;
//import com.xiaoniucode.etp.server.web.controller.proxy.request.*;
//import com.xiaoniucode.etp.server.web.controller.proxy.response.HttpProxyDTO;
//import com.xiaoniucode.etp.server.web.controller.proxy.response.TcpProxyDTO;
//import com.xiaoniucode.etp.server.web.service.ProxyService;
//import jakarta.validation.Valid;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/proxies")
//public class ProxyController {
//    @Autowired
//    private ProxyService proxyService;
//
//    @DeleteMapping
//    public Ajax batchDelete(@RequestBody BatchDeleteRequest request) {
//        proxyService.batchDeleteProxies(request);
//        return Ajax.success();
//    }
//
//    @PostMapping("tcp")
//    public Ajax addTcpProxy(@RequestBody TcpProxyCreateRequest request) {
//        proxyService.createTcpProxy(request);
//        return Ajax.success();
//    }
//
//    @PostMapping("http")
//    public Ajax addHttpProxy(@RequestBody HttpProxyCreateRequest request) {
//        proxyService.createHttpProxy(request);
//        return Ajax.success();
//    }
//
//    @PutMapping("tcp")
//    public Ajax updateTcpProxy(@RequestBody TcpProxyUpdateRequest request) {
//        proxyService.updateTcpProxy(request);
//        return Ajax.success();
//    }
//
//    @PutMapping("http")
//    public Ajax updateHttpProxy(@RequestBody HttpProxyUpdateRequest request) {
//        proxyService.updateHttpProxy(request);
//        return Ajax.success();
//    }
//
//    /**
//     * 删除代理
//     */
//    @DeleteMapping("{id}")
//    public Ajax deleteProxy(@PathVariable String id) {
//        proxyService.deleteProxy(id);
//        return Ajax.success();
//    }
//
//    /**
//     * 根据 ID 查询代理
//     */
//    @GetMapping("tcp/id")
//    public Ajax getTcpProxyById(@PathVariable String id) {
//        TcpProxyDTO proxy = proxyService.getTcpProxyById(id);
//        return Ajax.success(proxy);
//
//    }
//
//    /**
//     * 根据 ID 查询代理
//     */
//    @GetMapping("http/{id}")
//    public Ajax getHttpProxyById(@PathVariable String id) {
//        HttpProxyDTO proxy = proxyService.getHttpProxyById(id);
//        return Ajax.success(proxy);
//
//    }
//
//    /**
//     * 获取所有 TCP 代理
//     */
//    @GetMapping("tcp")
//    public Ajax getTcpProxies(@RequestParam(required = false) String keyword,
//                              @RequestParam(defaultValue = "1") int page,
//                              @RequestParam(defaultValue = "10") int size) {
//        List<TcpProxyDTO> proxies = proxyService.getTcpProxies(keyword,page,size);
//        return Ajax.success(proxies);
//    }
//
//    /**
//     * 获取所有 HTTP 代理
//     */
//    @GetMapping("http")
//    public Ajax getHttpProxies(@RequestParam(required = false) String keyword,
//                               @RequestParam(defaultValue = "1") int page,
//                               @RequestParam(defaultValue = "10") int size) {
//        List<HttpProxyDTO> proxies = proxyService.getHttpProxies(keyword,page,size);
//        return Ajax.success(proxies);
//    }
//
//    @PutMapping("{id}/status")
//    public Ajax setStatus(@PathVariable String id, @Valid @RequestBody StatusUpdateRequest request) {
//        proxyService.setProxyStatus(id, request.getEnabled());
//        return Ajax.success();
//    }
//
//}
