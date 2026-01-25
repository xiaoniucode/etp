package com.xiaoniucode.etp.server.manager;


import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.config.ConfigHelper;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(PortManager.class);
    private static final Set<Integer> allocatedPorts = new HashSet<>(32);
    private static int startPort = 1024;
    private static int endPort = 49151;
    private static final AtomicBoolean init = new AtomicBoolean(false);

    /**
     * 初始化端口范围,默认范围：1024-49151
     */
    public static void init(AppConfig appConfig) {
        if (init.get()) {
            init.set(true);
            return;
        }
        PortRange portRange = appConfig.getPortRange();
        startPort = portRange.getStart();
        endPort = portRange.getEnd();


        if (startPort == -1) {
            startPort = 1024;
        }
        if (endPort == -1) {
            endPort = 49151;
        }
        if (startPort < 1 || endPort < 1 || endPort > 65535 || startPort > endPort) {
            throw new IllegalArgumentException("无效的端口范围: " + startPort + "-" + endPort);
        }

        LOGGER.debug("端口分配器初始化，范围: {}-{}", startPort, endPort);
    }

    public static int acquire() {
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
                LOGGER.info("成功分配端口: {}", port);
                return port;
            }
        }

        // 随机分配失败，尝试顺序查找
        LOGGER.warn("随机分配失败，尝试顺序查找");
        for (int port = startPort; port <= endPort; port++) {
            if (allocatedPorts.contains(port)) {
                continue;
            }

            if (tryBindPort(port)) {
                allocatedPorts.add(port);
                return port;
            }
        }

        LOGGER.error("范围内所有端口都不可用");
        return -1;
    }

    private static boolean tryBindPort(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean release(int port) {
        if (allocatedPorts.remove(port)) {
            LOGGER.info("成功释放端口: {}", port);
            return true;
        } else {
            LOGGER.warn("尝试释放未分配的端口: {}", port);
            return false;
        }
    }

    public static boolean isPortAvailable(int port) {
        if (port < startPort || port > endPort) {
            LOGGER.warn("端口 {} 不在允许范围 {}-{} 内", port, startPort, endPort);
            return false;
        }
        if (allocatedPorts.contains(port)) {
            return false;
        }
        return tryBindPort(port);
    }

    public static void addRemotePort(Integer port) {
        allocatedPorts.add(port);
    }

}
