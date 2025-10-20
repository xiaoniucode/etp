package cn.xilio.etp.common;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 端口分配管理类
 * 功能：从1024-65535分配可用端口，排除常用端口，检查端口占用
 */
public class PortAllocator {
    // 存储被排除的端口
    private final Set<Integer> excludedPorts;
    // 存储已分配的端口及其用户标识
    private final ConcurrentHashMap<Integer, String> allocatedPorts;
    // 随机数生成器
    private final Random random;
    // 端口范围
    private static final int MIN_PORT = 1024;
    private static final int MAX_PORT = 65535;

    /**
     * 构造函数，初始化默认排除的常用端口
     */
    public PortAllocator() {
        this.excludedPorts = new HashSet<>();
        this.allocatedPorts = new ConcurrentHashMap<>();
        this.random = new Random();
        // 初始化默认排除的常用端口
        initializeDefaultExcludedPorts();
    }

    /**
     * 初始化默认排除的常用端口
     */
    private void initializeDefaultExcludedPorts() {
        int[] defaultExcluded = {
            3306,  // MySQL
            8080,  // Tomcat/HTTP
            6379,  // Redis
            80,    // HTTP
            443,   // HTTPS
            5432,  // PostgreSQL
            27017, // MongoDB
            21,    // FTP
            22,    // SSH
            25,    // SMTP
            3389   // RDP
        };
        for (int port : defaultExcluded) {
            excludedPorts.add(port);
        }
    }

    /**
     * 检查端口是否被占用
     * @param port 待检查的端口
     * @return true if port is in use, false otherwise
     */
    private boolean isPortInUse(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            socket.setReuseAddress(true);
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    /**
     * 为用户分配一个可用端口
     * @param userId 用户标识
     * @return 分配的端口号，-1表示无可用端口
     */
    public synchronized int allocatePort(String userId) {
        // 计算最大尝试次数，避免无限循环
        int maxAttempts = MAX_PORT - MIN_PORT - excludedPorts.size() - allocatedPorts.size();
        if (maxAttempts <= 0) {
            return -1; // 无可用端口
        }

        int attempts = 0;
        while (attempts < maxAttempts) {
            // 随机生成端口号
            int port = MIN_PORT + random.nextInt(MAX_PORT - MIN_PORT + 1);

            // 检查端口是否有效
            if (!excludedPorts.contains(port) && !allocatedPorts.containsKey(port) && !isPortInUse(port)) {
                allocatedPorts.put(port, userId);
                return port;
            }
            attempts++;
        }
        return -1; // 尝试多次后仍无可用端口
    }

    /**
     * 释放已分配的端口
     * @param port 要释放的端口
     * @return true if released successfully, false otherwise
     */
    public synchronized boolean releasePort(int port) {
        if (port < MIN_PORT || port > MAX_PORT) {
            return false;
        }
        return allocatedPorts.remove(port) != null;
    }

    /**
     * 添加排除端口
     * @param port 要排除的端口
     * @return true if added successfully, false if port is invalid
     */
    public synchronized boolean addExcludedPort(int port) {
        if (port < MIN_PORT || port > MAX_PORT) {
            return false;
        }
        return excludedPorts.add(port);
    }

    /**
     * 移除排除端口
     * @param port 要移除的排除端口
     * @return true if removed successfully, false otherwise
     */
    public synchronized boolean removeExcludedPort(int port) {
        return excludedPorts.remove(port);
    }

    /**
     * 获取所有排除端口
     * @return 排除端口集合
     */
    public Set<Integer> getExcludedPorts() {
        return new HashSet<>(excludedPorts);
    }

    /**
     * 获取所有已分配端口
     * @return 已分配端口映射
     */
    public ConcurrentHashMap<Integer, String> getAllocatedPorts() {
        return new ConcurrentHashMap<>(allocatedPorts);
    }

    /**
     * 检查指定端口是否可用
     * @param port 要检查的端口
     * @return true if port is available, false otherwise
     */
    public boolean isPortAvailable(int port) {
        if (port < MIN_PORT || port > MAX_PORT) {
            return false;
        }
        return !excludedPorts.contains(port) && !allocatedPorts.containsKey(port) && !isPortInUse(port);
    }

    /**
     * 清空所有分配记录
     */
    public synchronized void clearAllocatedPorts() {
        allocatedPorts.clear();
    }

    /**
     * 获取指定用户的已分配端口
     * @param userId 用户标识
     * @return 用户的已分配端口集合
     */
    public Set<Integer> getPortsByUser(String userId) {
        Set<Integer> userPorts = new HashSet<>();
        allocatedPorts.forEach((port, id) -> {
            if (id.equals(userId)) {
                userPorts.add(port);
            }
        });
        return userPorts;
    }

    /**
     * 测试用主方法
     */
    public static void main(String[] args) {
        PortAllocator allocator = new PortAllocator();

        // 测试端口分配
        String userId = "user123";
        int port = allocator.allocatePort(userId);
        if (port != -1) {
            System.out.println("Allocated port for " + userId + ": " + port);
        } else {
            System.out.println("No available port for " + userId);
        }

        // 测试添加排除端口
        allocator.addExcludedPort(9999);
        System.out.println("Excluded ports: " + allocator.getExcludedPorts());

        // 测试释放端口
        if (allocator.releasePort(port)) {
            System.out.println("Port " + port + " released successfully");
        }

        // 测试查询用户端口
        System.out.println("Ports for " + userId + ": " + allocator.getPortsByUser(userId));
    }
}
