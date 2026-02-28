package com.xiaoniucode.etp.server.web.common;

/**
 * 字节格式化
 */
public class ByteUtils {
    /**
     * 将字节数格式化为带单位的字符串（KB/MB/GB/TB）
     */
    public static String formatBytes(long bytes) {
        if (bytes <= 0) return "0MB";

        double value = bytes;
        String unit = "B";

        if (value >= 1024L * 1024 * 1024 * 1024L) {
            value /= (1024.0 * 1024 * 1024 * 1024);
            unit = "TB";
        } else if (value >= 1024L * 1024 * 1024) {
            value /= (1024.0 * 1024 * 1024);
            unit = "GB";
        } else if (value >= 1024L * 1024) {
            value /= (1024.0 * 1024);
            unit = "MB";
        } else if (value >= 1024L) {
            value /= 1024.0;
            unit = "KB";
        }

        // 处理小数，没有实际小数时只显示整数
        if (value == Math.floor(value)) {
            return (int) value + unit;
        } else {
            // 保留两位小数，但去掉末尾的0
            String formatted = String.format("%.2f", value).replaceAll("\\.?0*$", "");
            return formatted + unit;
        }
    }
}
