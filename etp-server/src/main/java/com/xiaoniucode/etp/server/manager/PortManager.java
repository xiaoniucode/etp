package com.xiaoniucode.etp.server.manager;


import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.config.domain.PortRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 端口分配管理器
 *
 * @author liuxin
 */
public class PortManager {
    private final Logger logger = LoggerFactory.getLogger(PortManager.class);
    private final Set<Integer> allocatedPorts = new HashSet<>(32);
    private int startPort = 1;
    private int endPort = 65535;
    private final AtomicBoolean init = new AtomicBoolean(false);

    public PortManager(AppConfig config) {
        if (init.get()) {
            init.set(true);
            return;
        }
        PortRange portRange = config.getPortRange();
        startPort = portRange.getStart();
        endPort = portRange.getEnd();


        if (startPort == -1) {
            startPort = 1;
        }
        if (endPort == -1) {
            endPort = 65535;
        }
        if (startPort < 1 || endPort < 1 || endPort > 65535 || startPort > endPort) {
            throw new IllegalArgumentException("无效的端口范围: " + startPort + "-" + endPort);
        }

        logger.debug("端口分配器初始化，范围: {}-{}", startPort, endPort);
    }
    public int acquire() {
        // 检查端口是否足够
        int totalPorts = endPort - startPort + 1;
        if (allocatedPorts.size() >= totalPorts) {
            throw new IllegalArgumentException("可用端口已用完！");
        }
        Random random = new Random();
        int rangeSize = endPort - startPort + 1;
        // 最多尝试20次
        for (int i = 0; i < 20; i++) {
            int port = startPort + random.nextInt(rangeSize);

            if (allocatedPorts.contains(port)) {
                continue;
            }
            if (tryBindPort(port)) {
                allocatedPorts.add(port);
                logger.info("成功分配端口: {}", port);
                return port;
            }
        }

        // 随机分配失败，尝试顺序查找
        logger.warn("随机分配失败，尝试顺序查找");
        for (int port = startPort; port <= endPort; port++) {
            if (allocatedPorts.contains(port)) {
                continue;
            }

            if (tryBindPort(port)) {
                allocatedPorts.add(port);
                return port;
            }
        }

        logger.error("范围内所有端口都不可用");
        return -1;
    }

    private boolean tryBindPort(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean release(int port) {
        if (allocatedPorts.remove(port)) {
            logger.info("成功释放端口: {}", port);
            return true;
        } else {
            logger.warn("尝试释放未分配的端口: {}", port);
            return false;
        }
    }

    public boolean isAvailable(int port) {
        if (port < startPort || port > endPort) {
            logger.warn("端口 {} 不在允许范围 {}-{} 内", port, startPort, endPort);
            return false;
        }
        if (allocatedPorts.contains(port)) {
            return false;
        }
        return tryBindPort(port);
    }

    public void addPort(Integer port) {
        allocatedPorts.add(port);
    }

}
