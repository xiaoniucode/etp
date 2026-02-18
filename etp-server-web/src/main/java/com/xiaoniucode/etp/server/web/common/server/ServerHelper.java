package com.xiaoniucode.etp.server.web.common.server;

import com.sun.management.OperatingSystemMXBean;
import com.xiaoniucode.etp.server.web.common.server.domain.CpuInfo;
import com.xiaoniucode.etp.server.web.common.server.domain.JvmMemoryInfo;
import com.xiaoniucode.etp.server.web.common.server.domain.OsMemoryInfo;
import com.xiaoniucode.etp.server.web.common.server.domain.ServerInfo;
import com.xiaoniucode.etp.server.web.common.ByteUtils;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 获取物理内存利用率、JVM内存利用率以及CPU利用率等信息
 */
public class ServerHelper {
    /**
     * 获取物理内存信息
     */
    private static final OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    /**
     * 获取JVM内存信息
     */
    private static final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    /**
     * 获取运行时信息
     */
    private static final RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
    /**
     * 获取Runtime信息
     */
    private static final Runtime runtime = Runtime.getRuntime();

    /**
     * CPU计算历史值
     */
    private static final AtomicLong prevProcessCpuTimeNs = new AtomicLong(0);
    private static final AtomicLong prevUptimeMs = new AtomicLong(0);

    /**
     * 获取JVM内存信息
     */
    public static JvmMemoryInfo getJvmMemory() {
        JvmMemoryInfo jvmMemory = new JvmMemoryInfo();
        MemoryUsage heap = memoryBean.getHeapMemoryUsage();

        long total = heap.getMax() > 0 ? heap.getMax() : heap.getCommitted();
        long used = heap.getUsed();
        double usage = total > 0 ? (used * 100.0) / total : 0;

        double rounded = Math.round(usage * 10) / 10.0;
        Object usageValue;
        if (rounded == Math.floor(rounded)) {
            usageValue = (int) rounded;
        } else {
            String strValue = String.valueOf(rounded);
            if (strValue.endsWith(".0")) {
                usageValue = (int) rounded;
            } else {
                usageValue = rounded;
            }
        }

        jvmMemory.setTotal(ByteUtils.formatBytes(total));
        jvmMemory.setUsed(ByteUtils.formatBytes(used));
        jvmMemory.setUsage(usageValue);
        return jvmMemory;
    }

    /**
     * 获取物理内存信息
     */
    public static OsMemoryInfo getOsMemory() {
        OsMemoryInfo osMemory = new OsMemoryInfo();

        long total = osBean.getTotalMemorySize();
        long free = osBean.getFreeMemorySize();
        long used = total - free;
        double usage = total > 0 ? (used * 100.0) / total : 0;

        double rounded = Math.round(usage * 10) / 10.0;
        Object usageValue;
        if (rounded == Math.floor(rounded)) {
            usageValue = (int) rounded;
        } else {
            String strValue = String.valueOf(rounded);
            if (strValue.endsWith(".0")) {
                usageValue = (int) rounded;
            } else {
                usageValue = rounded;
            }
        }

        osMemory.setTotal(ByteUtils.formatBytes(total));
        osMemory.setUsed(ByteUtils.formatBytes(used));
        osMemory.setUsage(usageValue);
        return osMemory;
    }

    public static CpuInfo getCpu() {
        CpuInfo cpu = new CpuInfo();
        int cores = runtime.availableProcessors();

        long currentCpuTimeNs = osBean.getProcessCpuTime();
        long currentUptimeMs = runtimeMxBean.getUptime();

        double usage = 0.0;

        long prevCpu = prevProcessCpuTimeNs.get();
        long prevUp = prevUptimeMs.get();

        //
        if (prevCpu != 0 && prevUp != 0 && currentUptimeMs > prevUp) {
            long uptimeDiffMs = currentUptimeMs - prevUp;
            long cpuTimeDiffNs = currentCpuTimeNs - prevCpu;

            double uptimeDiffSec = uptimeDiffMs / 1000.0;
            double cpuTimeDiffSec = cpuTimeDiffNs / 1_000_000_000.0;

            // 公式：进程CPU使用率（%）
            usage = (cpuTimeDiffSec / (uptimeDiffSec * cores)) * 100.0;
            usage = Math.max(0.0, Math.min(100.0, usage));
        }

        prevProcessCpuTimeNs.set(currentCpuTimeNs);
        prevUptimeMs.set(currentUptimeMs);

        double rounded = Math.round(usage * 10) / 10.0;
        Object usageValue;
        if (rounded == Math.floor(rounded)) {
            usageValue = (int) rounded;
        } else {
            String strValue = String.valueOf(rounded);
            if (strValue.endsWith(".0")) {
                usageValue = (int) rounded;
            } else {
                usageValue = rounded;
            }
        }

        cpu.setTotal(cores);
        cpu.setUsed(usageValue);
        cpu.setUsage(usageValue);

        return cpu;
    }

    public static ServerInfo getServerInfo() {
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setCpu(getCpu());
        serverInfo.setJvmMem(getJvmMemory());
        serverInfo.setOsMem(getOsMemory());
        return serverInfo;
    }
}
