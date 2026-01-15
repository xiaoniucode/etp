package com.xiaoniucode.etp.server.manager.port;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 端口分配器
 */
public class PortAllocator {
    private final Set<Integer> availablePorts;
    private final Set<Integer> allocatedPorts;
    private final int startPort;
    private final int endPort;
    private final ReadWriteLock lock;
    
    public PortAllocator(int startPort, int endPort) {
        this.startPort = startPort;
        this.endPort = endPort;
        this.availablePorts = ConcurrentHashMap.newKeySet();
        this.allocatedPorts = ConcurrentHashMap.newKeySet();
        this.lock = new ReentrantReadWriteLock();
        initializePool();
    }
    
    private void initializePool() {
        for (int port = startPort; port <= endPort; port++) {
            availablePorts.add(port);
        }
    }
    
    /**
     * 分配一个可用端口
     */
    public int allocatePort() {
        lock.writeLock().lock();
        try {
            if (availablePorts.isEmpty()) {
                throw new IllegalStateException("No available ports");
            }
            
            // 随机选择一个可用端口
            for (Integer port : availablePorts) {
                if (tryBindPort(port)) {
                    availablePorts.remove(port);
                    allocatedPorts.add(port);
                    return port;
                }
            }
            
            // 如果随机选择失败，尝试顺序分配
            for (int port = startPort; port <= endPort; port++) {
                if (availablePorts.contains(port) && tryBindPort(port)) {
                    availablePorts.remove(port);
                    allocatedPorts.add(port);
                    return port;
                }
            }
            
            return -1;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 释放端口
     */
    public boolean releasePort(int port) {
        lock.writeLock().lock();
        try {
            if (allocatedPorts.remove(port)) {
                availablePorts.add(port);
                return true;
            }
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 检查端口是否可用
     */
    public boolean isPortAvailable(int port) {
        lock.readLock().lock();
        try {
            return availablePorts.contains(port) && tryBindPort(port);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 尝试绑定端口
     */
    private boolean tryBindPort(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}