package com.xiaoniucode.etp.server.web.common;

import com.sun.management.OperatingSystemMXBean;
import org.json.JSONObject;

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
    public static JSONObject getJvmMemory() {
        JSONObject jvmMemory = new JSONObject();
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
        
        jvmMemory.put("total", ByteUtils.formatBytes(total));
        jvmMemory.put("used", ByteUtils.formatBytes(used));
        jvmMemory.put("usage", usageValue);
        return jvmMemory;
    }

    /**
     * 获取物理内存信息
     */
    public static JSONObject getOsMemory() {
        JSONObject osMemory = new JSONObject();
        
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
        
        osMemory.put("total", ByteUtils.formatBytes(total));
        osMemory.put("used", ByteUtils.formatBytes(used));
        osMemory.put("usage", usageValue);
        return osMemory;
    }

    public static JSONObject getCpu() {
        JSONObject cpu = new JSONObject();
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

        cpu.put("total", cores);
        cpu.put("used", usageValue);
        cpu.put("usage", usageValue);

        return cpu;
    }

    public static JSONObject getServerInfo() {
        JSONObject serverInfo = new JSONObject();
        serverInfo.put("cpu", getCpu());
        serverInfo.put("jvmMem", getJvmMemory());
        serverInfo.put("osMem", getOsMemory());
        return serverInfo;
    }
}
