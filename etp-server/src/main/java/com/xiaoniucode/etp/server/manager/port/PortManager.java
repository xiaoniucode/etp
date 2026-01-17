package com.xiaoniucode.etp.server.manager.port;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 端口管理器
 * 负责端口的分配、释放和管理
 */
public class PortManager {
    public static final int MIN_PORT = 1;
    public static final int MAX_PORT = 65535;
    public static final Duration MAX_PORT_RESERVED_DURATION = Duration.ofHours(24);
    public static final Duration CLEAN_RESERVED_PORTS_INTERVAL = Duration.ofHours(1);

    public static class PortAlreadyUsedException extends RuntimeException {
        public PortAlreadyUsedException() {
            super("port already used");
        }
    }

    public static class PortNotAllowedException extends RuntimeException {
        public PortNotAllowedException() {
            super("port not allowed");
        }
    }

    public static class PortUnAvailableException extends RuntimeException {
        public PortUnAvailableException() {
            super("port unavailable");
        }
    }

    public static class NoAvailablePortException extends RuntimeException {
        public NoAvailablePortException() {
            super("no available port");
        }
    }

    private final Map<String, PortContext> reservedPorts;
    private final Map<Integer, PortContext> usedPorts;
    private final Set<Integer> freePorts;

    private final String bindAddr;
    /**
     * tcp or udp
     */
    private final String netType;
    private final ReentrantLock lock;

    /**
     * 创建端口管理器实例
     *
     * @param netType    网络类型："tcp" 或 "udp"
     * @param bindAddr   绑定地址
     * @param allowPorts 允许使用的端口范围
     */
    public PortManager(String netType, String bindAddr, List<PortRange> allowPorts) {
        this.netType = netType;
        this.bindAddr = bindAddr;
        this.reservedPorts = new ConcurrentHashMap<>();
        this.usedPorts = new ConcurrentHashMap<>();
        this.freePorts = new CopyOnWriteArraySet<>();
        this.lock = new ReentrantLock();

        // 初始化空闲端口列表
        if (allowPorts != null && !allowPorts.isEmpty()) {
            for (PortRange range : allowPorts) {
                if (range.isSinglePort()) {
                    this.freePorts.add(range.getSingle());
                } else if (range.isPortRange()) {
                    for (int i = range.getStart(); i <= range.getEnd(); i++) {
                        this.freePorts.add(i);
                    }
                }
            }
        } else {
            // 默认允许所有端口
            for (int i = MIN_PORT; i <= MAX_PORT; i++) {
                this.freePorts.add(i);
            }
        }

        // 启动定时清理线程
        startCleanReservedPortsWorker();
    }

    /**
     * 获取端口
     *
     * @param proxyName 代理名称
     * @param port      请求的端口，0表示自动分配
     * @return 实际分配的端口
     * @throws PortAlreadyUsedException 端口已被使用
     * @throws PortNotAllowedException  端口不被允许
     * @throws PortUnAvailableException 端口不可用
     * @throws NoAvailablePortException 没有可用端口
     */
    public int acquire(String proxyName, int port) {
        PortContext portCtx = new PortContext(proxyName, 0);
        int realPort = 0;

        lock.lock();
        try {
            // 检查是否有预留端口
            if (port == 0) {
                PortContext reservedCtx = reservedPorts.get(proxyName);
                if (reservedCtx != null && isPortAvailable(reservedCtx.getPort())) {
                    realPort = reservedCtx.getPort();
                    usedPorts.put(realPort, portCtx);
                    reservedPorts.put(proxyName, portCtx);
                    freePorts.remove(realPort);
                    portCtx.setPort(realPort);
                    return realPort;
                }
            }

            if (port == 0) {
                // 自动分配端口，最多尝试5次
                int maxTryTimes = 5;
                int count = 0;
                List<Integer> portList = new ArrayList<>(freePorts);
                Collections.shuffle(portList);

                for (int candidatePort : portList) {
                    count++;
                    if (count > maxTryTimes) {
                        break;
                    }
                    if (isPortAvailable(candidatePort)) {
                        realPort = candidatePort;
                        usedPorts.put(realPort, portCtx);
                        reservedPorts.put(proxyName, portCtx);
                        freePorts.remove(realPort);
                        portCtx.setPort(realPort);
                        return realPort;
                    }
                }

                if (realPort == 0) {
                    throw new NoAvailablePortException();
                }
            } else {
                // 指定端口
                if (freePorts.contains(port)) {
                    if (isPortAvailable(port)) {
                        realPort = port;
                        usedPorts.put(realPort, portCtx);
                        reservedPorts.put(proxyName, portCtx);
                        freePorts.remove(realPort);
                        portCtx.setPort(realPort);
                        return realPort;
                    } else {
                        throw new PortUnAvailableException();
                    }
                } else {
                    if (usedPorts.containsKey(port)) {
                        throw new PortAlreadyUsedException();
                    } else {
                        throw new PortNotAllowedException();
                    }
                }
            }

            return realPort;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 释放端口
     *
     * @param port 要释放的端口
     */
    public void release(int port) {
        lock.lock();
        try {
            PortContext ctx = usedPorts.remove(port);
            if (ctx != null) {
                freePorts.add(port);
                ctx.setClosed(true);
                ctx.setUpdateTime(LocalDateTime.now());
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 检查端口是否可用
     *
     * @param port 要检查的端口
     * @return 端口是否可用
     */
    private boolean isPortAvailable(int port) {
        boolean isAvailable;
        try {
            if ("udp".equalsIgnoreCase(netType)) {
                // 检查UDP端口是否可用
                try (DatagramSocket socket = new DatagramSocket(port, InetAddress.getByName(bindAddr))) {
                    isAvailable = true;
                }
            } else {
                // 检查TCP端口是否可用
                try (ServerSocket socket = new ServerSocket(port, 0, InetAddress.getByName(bindAddr))) {
                    isAvailable = true;
                }
            }
        } catch (IOException e) {
            // 端口被占用
            isAvailable = false;
        }
        return isAvailable;
    }

    /**
     * 启动定时清理线程
     */
    private void startCleanReservedPortsWorker() {
        Thread worker = new Thread(() -> {
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(CLEAN_RESERVED_PORTS_INTERVAL.toSeconds());
                    cleanReservedPorts();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "PortCleanupWorker");
        worker.setDaemon(true);
        worker.start();
    }

    /**
     * 清理过期的保留端口
     */
    private void cleanReservedPorts() {
        lock.lock();
        try {
            LocalDateTime now = LocalDateTime.now();
            List<String> toRemove = new ArrayList<>();

            for (Map.Entry<String, PortContext> entry : reservedPorts.entrySet()) {
                PortContext ctx = entry.getValue();
                if (ctx.isClosed() && Duration.between(ctx.getUpdateTime(), now).compareTo(MAX_PORT_RESERVED_DURATION) > 0) {
                    toRemove.add(entry.getKey());
                }
            }

            // 移除过期的保留端口
            for (String proxyName : toRemove) {
                reservedPorts.remove(proxyName);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取当前使用中的端口数量
     */
    public int getUsedPortCount() {
        return usedPorts.size();
    }

    /**
     * 获取当前空闲端口数量
     */
    public int getFreePortCount() {
        return freePorts.size();
    }
}