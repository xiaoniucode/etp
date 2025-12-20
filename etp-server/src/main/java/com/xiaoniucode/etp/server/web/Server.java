package com.xiaoniucode.etp.server.web;

import com.xiaoniucode.etp.server.web.utils.Arith;

import com.xiaoniucode.etp.server.web.utils.DateUtils;
import com.xiaoniucode.etp.server.web.utils.IpUtils;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Properties;
import org.json.JSONArray;
import org.json.JSONObject;


import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.CentralProcessor.TickType;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.Util;

/**
 * 服务器相关信息
 *
 * @author liuxin
 */
public class Server {
    private static final int OSHI_WAIT_SECOND = 1000;

    /**
     * CPU相关信息
     */
    private final JSONObject cpu = new JSONObject();

    /**
     * 內存相关信息
     */
    private final JSONObject mem = new JSONObject();
    /**
     * JVM相关信息
     */
    private final JSONObject jvm = new JSONObject();
    /**
     * 服务器相关信息
     */
    private final JSONObject sys = new JSONObject();
    /**
     * 磁盘相关信息
     */
    private final JSONArray sysFiles = new JSONArray();

    /**
     * 获取完整的服务器信息JSONObject
     */
    public JSONObject getServerInfo() {
        JSONObject serverInfo = new JSONObject();
        copyTo();
        serverInfo.put("cpu", cpu);
        serverInfo.put("mem", mem);
        serverInfo.put("jvm", jvm);
        serverInfo.put("sys", sys);
        serverInfo.put("sysFiles", sysFiles);
        return serverInfo;
    }

    private void copyTo() {
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        setCpuInfo(hal.getProcessor());
        setMemInfo(hal.getMemory());
        setSysInfo();
        setJvmInfo();
        setSysFiles(si.getOperatingSystem());
    }

    /**
     * 设置CPU信息
     */
    private void setCpuInfo(CentralProcessor processor) {
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        Util.sleep(OSHI_WAIT_SECOND);
        long[] ticks = processor.getSystemCpuLoadTicks();
        long nice = ticks[TickType.NICE.getIndex()] - prevTicks[TickType.NICE.getIndex()];
        long irq = ticks[TickType.IRQ.getIndex()] - prevTicks[TickType.IRQ.getIndex()];
        long softirq = ticks[TickType.SOFTIRQ.getIndex()] - prevTicks[TickType.SOFTIRQ.getIndex()];
        long steal = ticks[TickType.STEAL.getIndex()] - prevTicks[TickType.STEAL.getIndex()];
        long cSys = ticks[TickType.SYSTEM.getIndex()] - prevTicks[TickType.SYSTEM.getIndex()];
        long user = ticks[TickType.USER.getIndex()] - prevTicks[TickType.USER.getIndex()];
        long iowait = ticks[TickType.IOWAIT.getIndex()] - prevTicks[TickType.IOWAIT.getIndex()];
        long idle = ticks[TickType.IDLE.getIndex()] - prevTicks[TickType.IDLE.getIndex()];
        long totalCpu = user + nice + cSys + idle + iowait + irq + softirq + steal;

        cpu.put("cpuNum", processor.getLogicalProcessorCount());
        cpu.put("total", Arith.round(Arith.mul(totalCpu, 100), 2));
        cpu.put("sys", Arith.round(Arith.mul((double) cSys / totalCpu, 100), 2));
        cpu.put("used", Arith.round(Arith.mul((double) user / totalCpu, 100), 2));
        cpu.put("wait", Arith.round(Arith.mul((double) iowait / totalCpu, 100), 2));
        cpu.put("free", Arith.round(Arith.mul((double) idle / totalCpu, 100), 2));
    }

    /**
     * 设置内存信息
     */
    private void setMemInfo(GlobalMemory memory) {
        mem.put("total", Arith.div(memory.getTotal(), (1024 * 1024 * 1024), 2));
        mem.put("used", Arith.div(memory.getTotal() - memory.getAvailable(), (1024 * 1024 * 1024), 2));
        mem.put("free", Arith.div(memory.getAvailable(), (1024 * 1024 * 1024), 2));
        double totalGb = Arith.div(memory.getTotal(), (1024 * 1024 * 1024), 2);
        double usedGb = Arith.div(memory.getTotal() - memory.getAvailable(), (1024 * 1024 * 1024), 2);
        mem.put("usage", Arith.mul(Arith.div(usedGb, totalGb, 4), 100));
    }

    /**
     * 设置服务器信息
     */
    private void setSysInfo() {
        Properties props = System.getProperties();
        sys.put("computerName", IpUtils.getHostName());
        sys.put("computerIp", IpUtils.getHostIp());
        sys.put("osName", props.getProperty("os.name"));
        sys.put("osArch", props.getProperty("os.arch"));
        sys.put("userDir", props.getProperty("user.dir"));
    }

    /**
     * 设置Java虚拟机
     */
    private void setJvmInfo() {
        Properties props = System.getProperties();
        String jdkName = ManagementFactory.getRuntimeMXBean().getVmName();
        String startTime = DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, DateUtils.getServerStartDate());
        String runTime = DateUtils.timeDistance(DateUtils.getNowDate(), DateUtils.getServerStartDate());
        String inputArts = ManagementFactory.getRuntimeMXBean().getInputArguments().toString();

        jvm.put("total", Arith.div(Runtime.getRuntime().totalMemory(), (1024 * 1024), 2));
        jvm.put("max", Arith.div(Runtime.getRuntime().maxMemory(), (1024 * 1024), 2));
        jvm.put("free", Arith.div(Runtime.getRuntime().freeMemory(), (1024 * 1024), 2));
        jvm.put("version", props.getProperty("java.version"));
        jvm.put("home", props.getProperty("java.home"));
        jvm.put("jdkName", jdkName);
        jvm.put("startTime", startTime);
        jvm.put("runTime", runTime);
        jvm.put("inputArts", inputArts);
        double totalMb = Arith.div(Runtime.getRuntime().totalMemory(), (1024 * 1024), 2);
        double freeMb = Arith.div(Runtime.getRuntime().freeMemory(), (1024 * 1024), 2);
        jvm.put("used", Arith.div(totalMb - freeMb, 1, 2));
        jvm.put("usage", Arith.mul(Arith.div(totalMb - freeMb, totalMb, 4), 100));
    }

    /**
     * 设置磁盘信息
     */
    private void setSysFiles(OperatingSystem os) {
        FileSystem fileSystem = os.getFileSystem();
        List<OSFileStore> fsArray = fileSystem.getFileStores();
        for (OSFileStore fs : fsArray) {
            long free = fs.getUsableSpace();
            long total = fs.getTotalSpace();
            long used = total - free;
            JSONObject sysFile = new JSONObject();
            sysFile.put("dirName", fs.getMount());
            sysFile.put("sysTypeName", fs.getType());
            sysFile.put("typeName", fs.getName());
            sysFile.put("total", convertFileSize(total));
            sysFile.put("free", convertFileSize(free));
            sysFile.put("used", convertFileSize(used));
            sysFile.put("usage", Arith.mul(Arith.div(used, total, 4), 100));
            sysFiles.put(sysFile);
        }
    }

    /**
     * 字节转换
     *
     * @param size 字节大小
     */
    public String convertFileSize(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;
        if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        } else {
            return String.format("%d B", size);
        }
    }
}
