package com.xiaoniucode.etp.server.manager;

import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.config.PortRange;
import com.xiaoniucode.etp.server.web.server.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * 端口分配管理器
 * todo 需要优化
 *
 * @author liuxin
 */
public class PortAllocator {
    private static final Logger LOGGER = LoggerFactory.getLogger(PortAllocator.class);
    private static final PortAllocator instance = new PortAllocator();
    private final Set<Integer> allocatedPorts = new HashSet<>(32);
    private final PortRange portRange = AppConfig.get().getPortRange();
    private int startPort;
    private int endPort;

    private PortAllocator() {
        initPortRange();
    }

    public static PortAllocator get() {
        return instance;
    }

    /**
     * 初始化端口范围：如果为-1则使用标准范围
     */
    private void initPortRange() {
        startPort = portRange.getStart();
        endPort = portRange.getEnd();

        // 标准范围：1024-49151
        if (startPort == -1) {
            startPort = 1024;
        }
        if (endPort == -1) {
            endPort = 49151;
        }

        // 验证范围
        if (startPort < 1 || startPort > 65535 || endPort < 1 || endPort > 65535 || startPort > endPort) {
            throw new IllegalArgumentException("无效的端口范围: " + startPort + "-" + endPort);
        }

        LOGGER.info("端口分配器初始化，范围: {}-{}", startPort, endPort);
    }

    public int allocateAvailablePort() {
        // 检查端口是否足够
        int totalPorts = endPort - startPort + 1;
        if (allocatedPorts.size() >= totalPorts) {
            throw new IllegalArgumentException("可用公网端口已用完！");
        }
        Random random = new Random();
        int rangeSize = endPort - startPort + 1;
        // 最多尝试50次
        for (int i = 0; i < 50; i++) {
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

    private boolean tryBindPort(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean releasePort(int port) {
        if (allocatedPorts.remove(port)) {
            LOGGER.info("成功释放端口: {}", port);
            return true;
        } else {
            LOGGER.warn("尝试释放未分配的端口: {}", port);
            return false;
        }
    }

    public boolean isPortAvailable(int port) {
        if (port < 1 || port > 65535) {
            String errorMsg = String.format("无效端口号: %d，必须在1到65535之间", port);
            LOGGER.error(errorMsg);
            return false;
        }
        if (port < startPort || port > endPort) {
            LOGGER.warn("端口 {} 不在允许范围 {}-{} 内", port, startPort, endPort);
            return false;
        }
        if (allocatedPorts.contains(port)) {
            return false;
        }
        return tryBindPort(port);
    }

    public void addRemotePort(Integer port) {
        allocatedPorts.add(port);
    }
}
