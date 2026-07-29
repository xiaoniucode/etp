package io.github.lxien.orbien.server.web.controller;

import io.github.lxien.orbien.server.web.common.message.Ajax;
import io.github.lxien.orbien.server.web.dto.transport.ProxyTransportDetailDTO;
import io.github.lxien.orbien.server.web.param.transport.TransportSaveParam;
import io.github.lxien.orbien.server.web.service.ProxyTransportService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/proxy-transport")
@Validated
public class ProxyTransportController {

    @Autowired
    private ProxyTransportService proxyTransportService;

    @GetMapping("/{proxyId}")
    public Ajax get(@PathVariable String proxyId) {
        ProxyTransportDetailDTO detail = proxyTransportService.getByProxyId(proxyId);
        return Ajax.success(detail);
    }

    @PutMapping("/{proxyId}")
    public Ajax save(@PathVariable String proxyId, @RequestBody @Valid TransportSaveParam param) {
        proxyTransportService.save(proxyId, param);
        return Ajax.success();
    }
}
