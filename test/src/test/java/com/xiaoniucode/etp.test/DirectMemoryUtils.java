package com.xiaoniucode.etp.test;

import java.lang.management.BufferPoolMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;

public final class DirectMemoryUtils {

    private static final String DIRECT = "direct";

    private DirectMemoryUtils() {
    }

    /**
     * 已使用 DirectMemory
     */
    public static long usedDirectMemory() {

        BufferPoolMXBean pool = getDirectPool();

        return pool == null ? -1 : pool.getMemoryUsed();
    }

    /**
     * DirectBuffer 数量
     */
    public static long directBufferCount() {

        BufferPoolMXBean pool = getDirectPool();

        return pool == null ? -1 : pool.getCount();
    }

    /**
     * 获取最大 DirectMemory
     */
    public static long maxDirectMemory() {

        RuntimeMXBean runtimeMXBean =
                ManagementFactory.getRuntimeMXBean();

        List<String> args = runtimeMXBean.getInputArguments();

        for (String arg : args) {

            if (arg.startsWith("-XX:MaxDirectMemorySize=")) {

                String value =
                        arg.substring("-XX:MaxDirectMemorySize=".length());

                return parseMemory(value);
            }
        }

        /*
         * HotSpot 默认：
         * MaxDirectMemorySize ≈ MaxHeapSize
         */
        return Runtime.getRuntime().maxMemory();
    }

    /**
     * 获取 direct pool
     */
    private static BufferPoolMXBean getDirectPool() {

        List<BufferPoolMXBean> pools =
                ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class);

        for (BufferPoolMXBean pool : pools) {

            if (DIRECT.equalsIgnoreCase(pool.getName())) {
                return pool;
            }
        }

        return null;
    }

    /**
     * 解析内存字符串
     *
     * 例如：
     * 256m
     * 2g
     * 1024k
     */
    private static long parseMemory(String value) {

        value = value.trim().toLowerCase();

        long multiplier = 1;

        if (value.endsWith("k")) {
            multiplier = 1024L;
            value = value.substring(0, value.length() - 1);
        } else if (value.endsWith("m")) {
            multiplier = 1024L * 1024L;
            value = value.substring(0, value.length() - 1);
        } else if (value.endsWith("g")) {
            multiplier = 1024L * 1024L * 1024L;
            value = value.substring(0, value.length() - 1);
        }

        return Long.parseLong(value) * multiplier;
    }

    /**
     * 格式化
     */
    public static String human(long bytes) {

        double value = bytes;

        String[] units = {"B", "KB", "MB", "GB", "TB"};

        int unit = 0;

        while (value >= 1024 && unit < units.length - 1) {
            value /= 1024;
            unit++;
        }

        return String.format("%.2f %s", value, units[unit]);
    }

    /**
     * 打印
     */
    public static void printStats() {

        long used = usedDirectMemory();
        long max = maxDirectMemory();
        long count = directBufferCount();

        System.out.println("========== Direct Memory ==========");
        System.out.println("Used  : " + human(used));
        System.out.println("Max   : " + human(max));
        System.out.println("Count : " + count);

        double usage = used * 100.0 / max;

        System.out.printf("Usage : %.2f%%%n", usage);

        System.out.println("===================================");
    }
}