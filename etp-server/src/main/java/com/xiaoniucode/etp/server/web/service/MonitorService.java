package com.xiaoniucode.etp.server.web.service;

import com.xiaoniucode.etp.server.web.common.server.domain.ServerInfo;

import java.util.Map;

public interface MonitorService {

    /**
     * 获取监控概览数据
     */
    Map<String, Object> getMonitorOverview();

    /**
     * 获取服务器监控数据
     */
    ServerInfo getServerMonitorData();

    /**
     * 获取流量数据
     */
    Map<String, Object> getTrafficData();
}
