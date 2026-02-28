package com.xiaoniucode.etp.core.domain;

import lombok.Getter;
import lombok.ToString;

@ToString
public class BandwidthConfig {
    /**
     * 总带宽限制
     * 单位（bps）
     */
    @Getter
    private String limit;
    /**
     * 入口带宽限制（下载）
     * 单位（bps）
     */
    @Getter
    private String limitIn;
    /**
     * 出口带宽限制（上传）
     * 单位（bps）
     */
    @Getter
    private String limitOut;
    private Long limitBytes;
    private Long limitInBytes;
    private Long limitOutBytes;
    private static final long KB = 1024;
    private static final long MB = 1024 * 1024;
    private static final long GB = 1024 * 1024 * 1024;

    public BandwidthConfig() {
    }

    public BandwidthConfig(String limit, String limitIn, String limitOut) {
        this.limit = limit;
        this.limitIn = limitIn;
        this.limitOut = limitOut;
        parseAndValidate();
    }

    /**
     * 解析并验证配置
     */
    public void parseAndValidate() {
        this.limitBytes = parseSize(limit);
        this.limitInBytes = parseSize(limitIn);
        this.limitOutBytes = parseSize(limitOut);

        //1.如果配置了方向限制，优先使用方向限制
        //2.limitIn + limitOut 不应该超过 limit
        if (limitBytes != null && limitInBytes != null && limitOutBytes != null) {
            if (limitInBytes + limitOutBytes > limitBytes) {
                throw new IllegalArgumentException(
                        String.format("配置错误: 入口(%s) + 出口(%s) = %s 超过总限制(%s)",
                                formatSize(limitInBytes),
                                formatSize(limitOutBytes),
                                formatSize(limitInBytes + limitOutBytes),
                                formatSize(limitBytes)
                        )
                );
            }
        }
        //3.单个方向不能超过总限制
        if (limitBytes != null) {
            if (limitInBytes != null && limitInBytes > limitBytes) {
                throw new IllegalArgumentException(
                        String.format("入口限制(%s)不能超过总限制(%s)", limitIn, limit));
            }
            if (limitOutBytes != null && limitOutBytes > limitBytes) {
                throw new IllegalArgumentException(
                        String.format("出口限制(%s)不能超过总限制(%s)", limitOut, limit));
            }
        }
    }

    /**
     * 解析带单位的字符串为字节数
     * 支持格式：512K, 1M, 2G, 1024（默认字节）
     */
    private Long parseSize(String size) {
        if (size == null || size.isEmpty()) {
            return null;
        }

        size = size.trim().toUpperCase();

        // 提取数字部分
        String numStr = size.replaceAll("[^0-9.]", "");
        if (numStr.isEmpty()) {
            throw new IllegalArgumentException("无效的带宽格式: " + size);
        }

        double value;
        try {
            value = Double.parseDouble(numStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("无效的数字格式: " + numStr);
        }

        // 提取单位
        String unit = size.replaceAll("[0-9.]", "").trim();

        long bytes;
        switch (unit) {
            case "K":
            case "KB":
                bytes = (long) (value * KB);
                break;
            case "M":
            case "MB":
                bytes = (long) (value * MB);
                break;
            case "G":
            case "GB":
                bytes = (long) (value * GB);
                break;
            case "": // 无单位，默认字节
                bytes = (long) value;
                break;
            default:
                throw new IllegalArgumentException("不支持的单位: " + unit);
        }

        return bytes;
    }

    /**
     * 格式化字节数为可读字符串
     */
    private String formatSize(long bytes) {
        if (bytes < KB) {
            return bytes + "B";
        } else if (bytes < MB) {
            return String.format("%.2fKB", bytes / (double) KB);
        } else if (bytes < GB) {
            return String.format("%.2fMB", bytes / (double) MB);
        } else {
            return String.format("%.2fGB", bytes / (double) GB);
        }
    }

    /**
     * 获取有效的入口限速
     * 优先级：limitIn > limit > 无限制
     */
    public Long getEffectiveInLimit() {
        if (limitInBytes != null) {
            return limitInBytes;
        }
        return limitBytes;
    }

    /**
     * 获取有效的出口限速
     * 优先级：limitOut > limit > 无限制
     */
    public Long getEffectiveOutLimit() {
        if (limitOutBytes != null) {
            return limitOutBytes;
        }
        return limitBytes;
    }

    /**
     * 检查是否有限速配置
     */
    public boolean hasLimit() {
        return limitBytes != null || limitInBytes != null || limitOutBytes != null;
    }

    /**
     * 检查入口是否限速
     */
    public boolean hasInLimit() {
        return getEffectiveInLimit() != null;
    }

    /**
     * 检查出口是否限速
     */
    public boolean hasOutLimit() {
        return getEffectiveOutLimit() != null;
    }

    public void setLimit(String limit) {
        this.limit = limit;
        parseAndValidate();
    }

    public void setLimitIn(String limitIn) {
        this.limitIn = limitIn;
        parseAndValidate();
    }

    public void setLimitOut(String limitOut) {
        this.limitOut = limitOut;
        parseAndValidate();
    }
}
