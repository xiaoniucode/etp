//package com.xiaoniucode.etp.server.web.controller.proxy;
//
//import com.xiaoniucode.etp.server.web.common.Ajax;
//import com.xiaoniucode.etp.server.web.controller.proxy.request.CreateHttpProxyRequest;
//import com.xiaoniucode.etp.server.web.controller.proxy.request.CreateHttpsProxyRequest;
//import com.xiaoniucode.etp.server.web.controller.proxy.request.CreateTcpProxyRequest;
//import com.xiaoniucode.etp.server.web.controller.proxy.request.DeleteProxyRequest;
//import com.xiaoniucode.etp.server.web.controller.proxy.request.GetProxyRequest;
//import com.xiaoniucode.etp.server.web.controller.proxy.request.SwitchProxyStatusRequest;
//import com.xiaoniucode.etp.server.web.controller.proxy.request.UpdateTcpProxyRequest;
//import com.xiaoniucode.etp.server.web.service.ProxyService;
//import jakarta.validation.Valid;
//import org.springframework.web.bind.annotation.*;
//
///**
// * 代理配置接口控制器
// *
// * @author liuxin
// */
//@RestController
//@RequestMapping("/proxies")
//public class ProxyController {
//
//    private final ProxyService proxyService;
//
//    public ProxyController(ProxyService proxyService) {
//        this.proxyService = proxyService;
//    }
//
//    @GetMapping
//    public Ajax getProxy(@Valid GetProxyRequest param) {
//        return Ajax.success(proxyService.getProxy(param));
//    }
//
//    @GetMapping("/list")
//    public Ajax getProxyList(@RequestParam("type") String type) {
//        return Ajax.success(proxyService.proxies(type));
//    }
//
//    @PostMapping("/tcp")
//    public Ajax createTcpProxy(@Valid @RequestBody CreateTcpProxyRequest param) {
//        proxyService.addTcpProxy(param);
//        return Ajax.success("ok");
//    }
//
//    @PostMapping("/http")
//    public Ajax createHttpProxy(@Valid @RequestBody CreateHttpProxyRequest param) {
//        proxyService.addHttpProxy(param);
//        return Ajax.success("ok");
//    }
//
//    @PostMapping("/https")
//    public Ajax createHttpsProxy(@Valid @RequestBody CreateHttpsProxyRequest param) {
//        proxyService.addHttpsProxy(param);
//        return Ajax.success("ok");
//    }
//
//    @PutMapping("/tcp")
//    public Ajax updateTcpProxy(@Valid @RequestBody UpdateTcpProxyRequest param) {
//        proxyService.updateTcpProxy(param);
//        return Ajax.success("ok");
//    }
//
//    @PatchMapping("/status")
//    public Ajax switchProxyStatus(@Valid @RequestBody SwitchProxyStatusRequest param) {
//        proxyService.switchProxyStatus(param);
//        return Ajax.success("ok");
//    }
//
//    @DeleteMapping
//    public Ajax deleteProxy(@Valid @RequestBody DeleteProxyRequest param) {
//        proxyService.deleteProxy(param);
//        return Ajax.success("ok");
//    }
//}
