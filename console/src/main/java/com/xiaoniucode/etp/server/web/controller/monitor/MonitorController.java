package com.xiaoniucode.etp.server.web.controller.monitor;

import com.xiaoniucode.etp.server.web.common.Ajax;
import com.xiaoniucode.etp.server.web.service.MonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/monitor")
public class MonitorController {

    @Autowired
    private MonitorService monitorService;

    /**
     * 获取监控概览数据
     */
    @GetMapping()
    public Ajax getMonitorOverview() {
        return Ajax.success(monitorService.getMonitorOverview());
    }
    
    /**
     * 获取服务器监控数据
     */
    @GetMapping("server")
    public Ajax getServerMonitorData() {
        return Ajax.success(monitorService.getServerMonitorData());
    }
    
    /**
     * 获取流量数据
     */
    @GetMapping("metrics/summery")
    public Ajax getTrafficData() {
        return Ajax.success(monitorService.getTrafficData());
    }
}
