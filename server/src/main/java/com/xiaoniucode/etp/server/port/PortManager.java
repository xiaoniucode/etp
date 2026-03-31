/*
 *    Copyright 2026 xiaoniucode
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.xiaoniucode.etp.server.port;

import com.xiaoniucode.etp.server.exceptions.EtpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.BitSet;
import java.util.Random;

/**
 * 端口分配管理器
 *
 * @author xiaoniucode
 *
 */
public class PortManager {
    private final Logger logger = LoggerFactory.getLogger(PortManager.class);

    private final BitSet allocatedPorts;
    private int startPort = 1;
    private final int endPort;
    private final int portCount;
    private final Random random = new Random();

    public PortManager(int start, int end) {
        startPort = Math.max(start, 1);
        endPort = Math.min(end, 65535);

        if (endPort < 1 || startPort > endPort) {
            throw new IllegalArgumentException("无效的端口范围: " + startPort + "-" + endPort);
        }
        portCount = endPort - startPort + 1;
        allocatedPorts = new BitSet(portCount);

        logger.debug("端口分配器初始化，范围: {}-{}", startPort, endPort);
    }

    public Integer acquire() {
        int totalAllocated = allocatedPorts.cardinality();
        if (totalAllocated >= portCount) {
            throw new EtpException("无可用端口");
        }
        // 随机尝试最多20次
        for (int i = 0; i < 20; i++) {
            int offset = random.nextInt(portCount);
            int absPort = startPort + offset;
            if (allocatedPorts.get(offset)) continue;
            if (tryBindPort(absPort)) {
                allocatedPorts.set(offset);
                logger.debug("端口分配成功: {}", absPort);
                return absPort;
            }
        }
        logger.warn("随机分配端口失败，尝试顺序查找");
        for (int offset = 0; offset < portCount; offset++) {
            if (allocatedPorts.get(offset)) continue;
            int absPort = startPort + offset;
            if (tryBindPort(absPort)) {
                allocatedPorts.set(offset);
                logger.debug("端口分配成功: {}", absPort);
                return absPort;
            }
        }
        logger.error("范围内所有端口都不可用");
        return null;
    }

    private boolean tryBindPort(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean release(int port) {
        if (port < startPort || port > endPort) {
            logger.warn("释放端口超出范围: {}", port);
            return false;
        }
        int offset = port - startPort;
        if (allocatedPorts.get(offset)) {
            allocatedPorts.clear(offset);
            logger.debug("释放端口占用: {}", port);
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
        int offset = port - startPort;
        if (allocatedPorts.get(offset)) {
            return false;
        }
        return tryBindPort(port);
    }

    public void addPort(Integer port) {
        if (port < startPort || port > endPort) {
            logger.warn("addPort: 端口 {} 不在范围 {}-{}", port, startPort, endPort);
            return;
        }
        int offset = port - startPort;
        allocatedPorts.set(offset);
    }
}