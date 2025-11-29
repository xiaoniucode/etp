package cn.xilio.etp.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantLock;


/**
 * 端口分配器，用于动态分配用户指定范围内的可用端口，并缓存已分配端口。
 *
 * @author liuxin
 */
public class PortAllocator {
    private static final Logger LOGGER = LoggerFactory.getLogger(PortAllocator.class.getName());
    private static volatile PortAllocator instance;
    private final ReentrantLock lock = new ReentrantLock();
    private final Set<Integer> allocatedPorts = new HashSet<>(32);

    private PortAllocator() {
    }

    public static PortAllocator getInstance() {
        if (instance == null) {
            synchronized (PortAllocator.class) {
                if (instance == null) {
                    instance = new PortAllocator();
                }
            }
        }
        return instance;
    }

    public int allocateAvailablePort() throws IOException {
        LOGGER.info("开始分配系统自动选择的可用端口");
        lock.lock();
        try {
            // 最多尝试10次，避免无限循环
            for (int i = 0; i < 10; i++) {
                try (ServerSocket socket = new ServerSocket(0)) {
                    int port = socket.getLocalPort();
                    if (!allocatedPorts.contains(port)) {
                        allocatedPorts.add(port);
                        LOGGER.info("成功分配系统端口: {}", port);
                        return port;
                    }
                } catch (IOException e) {
                    LOGGER.warn("系统端口分配失败，重试: {}/10", i + 1);
                }
            }
            LOGGER.error("无法分配系统端口，尝试次数耗尽");
            throw new IOException("无法分配系统端口：无可用端口");
        } finally {
            lock.unlock();
        }
    }
    public boolean releasePort(int port) {
        lock.lock();
        try {
            if (allocatedPorts.remove(port)) {
                LOGGER.info("成功释放端口: {}", port);
                return true;
            } else {
                LOGGER.warn("尝试释放未分配的端口: {}", port);
                return false;
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean isPortAllocated(int port) {
        lock.lock();
        try {
            return allocatedPorts.contains(port);
        } finally {
            lock.unlock();
        }
    }

    public boolean isPortAvailable(int port) {
        // 验证端口号
        if (port < 1 || port > 65535) {
            String errorMsg = String.format("无效端口号: %d，必须在1到65535之间", port);
            LOGGER.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        LOGGER.info("开始检查端口可用性: {}", port);
        lock.lock();
        try {
            // 检查是否已被本实例分配
            if (allocatedPorts.contains(port)) {
                LOGGER.info("端口 {} 已被分配，不可使用", port);
                return false;
            }

            // 尝试绑定端口
            try (ServerSocket socket = new ServerSocket(port)) {
                LOGGER.info("端口 {} 可用", port);
                return true;
            } catch (IOException e) {
                LOGGER.warn("端口 {} 不可用: {1}", new Object[]{port, e.getMessage()});
                return false;
            }
        } finally {
            lock.unlock();
        }
    }

    public void addRemotePort(Integer port) {
        allocatedPorts.add(port);
    }
}
