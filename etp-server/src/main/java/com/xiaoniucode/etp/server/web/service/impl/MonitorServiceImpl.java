package com.xiaoniucode.etp.server.web.service.impl;

import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.web.common.server.ServerHelper;
import com.xiaoniucode.etp.server.web.common.server.domain.ServerInfo;
import com.xiaoniucode.etp.server.web.service.MonitorService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MonitorServiceImpl implements MonitorService {
    @Resource
    private AppConfig config;

    /**
     * 获取监控概览数据（静态数据）
     */
    @Override
    public Map<String, Object> getMonitorOverview() {


        Map<String, Object> response = new HashMap<>();

        // 监控统计数据
        Map<String, Object> stats = new HashMap<>();
        stats.put("clientTotal", 15);
        stats.put("onlineClient", 8);

        Map<String, Object> proxy = new HashMap<>();
        proxy.put("total", 20);
        stats.put("proxy", proxy);

        stats.put("runningTunnel", 12);

        // 系统配置
        Map<String, Object> sysConfig = new HashMap<>();
        sysConfig.put("serverAddr", config.getServerAddr());
        sysConfig.put("serverPort", config.getServerPort());
        sysConfig.put("httpProxyPort",config.getHttpProxyPort());
        sysConfig.put("port_range_start", config.getPortRange().getStart());
        sysConfig.put("port_range_end", config.getPortRange().getEnd());
        sysConfig.put("tls_enabled", config.getTls().isEnable()?"开启":"未开启");
        sysConfig.put("baseDomains", config.getBaseDomains());

        response.put("stats", stats);
        response.put("sysConfig", sysConfig);

        return response;
    }

    /**
     * 获取服务器监控数据
     */
    @Override
    public ServerInfo getServerMonitorData() {

        return ServerHelper.getServerInfo();


    }

    /**
     * 获取流量数据
     */
    @Override
    public Map<String, Object> getTrafficData() {
        Map<String, Object> response = new HashMap<>();

        response.put("in", 12580);
        response.put("out", 8920);

        return response;
    }
}
