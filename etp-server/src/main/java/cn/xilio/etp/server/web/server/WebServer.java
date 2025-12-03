package cn.xilio.etp.server.web.server;

import cn.xilio.etp.core.Lifecycle;

/**
 * @author liuxin
 */
public interface WebServer extends Lifecycle {
    /**
     * 获取web服务端口号
     *
     * @return 端口号
     */
    int getPort();
}
