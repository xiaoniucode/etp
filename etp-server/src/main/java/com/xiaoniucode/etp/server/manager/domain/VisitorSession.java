package com.xiaoniucode.etp.server.manager.domain;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.domain.Target;
import com.xiaoniucode.etp.server.loadbalance.LoadBalancer;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VisitorSession {
    private String sessionId;
    /**
     * 访问者连接，公网用户访问时赋值
     */
    private Channel visitor;
    /**
     * 控制隧道，用于处理各种控制指令，Agent连接共享
     */
    private Channel control;
    /**
     * 数据传输隧道，连接成功后赋值，每个visitor一个，
     */
    private Channel tunnel;
    /**
     * 代理配置信息
     */
    private ProxyConfig proxyConfig;

    /**
     * 当前选择的目标服务器
     */
    private Target currentTarget;
    /**
     * 当前使用的负载均衡器
     */
    private LoadBalancer currentLoadBalancer;

    /**
     * 标记连接数是否已减少，避免重复减少
     */
    private boolean connectionCountDecremented = false;

    /**
     * 判断是否使用了负载均衡器
     */
    public boolean isUsingLoadBalancer() {
        return currentLoadBalancer != null;
    }
}
