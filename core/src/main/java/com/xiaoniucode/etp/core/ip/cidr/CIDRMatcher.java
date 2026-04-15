package com.xiaoniucode.etp.core.ip.cidr;

import com.xiaoniucode.etp.core.enums.AccessControlMode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 基于区间+二分的CIDR IP地址匹配器
 */
public class CIDRMatcher {

    @Getter
    private final List<CIDRRange> ranges;
    private final boolean isAllowMode;

    /**
     * 构建区间列表
     *
     * @param mode      匹配模式
     * @param allowList 白名单模式下的允许 IP 列表
     * @param denyList  黑名单模式下的拒绝 IP 列表
     */
    public CIDRMatcher(AccessControlMode mode, Set<String> allowList, Set<String> denyList) {
        this.isAllowMode = AccessControlMode.ALLOW.equals(mode);
        List<CIDRRange> tempRanges = new ArrayList<>();

        Set<String> targetList = isAllowMode ? allowList : denyList;
        if (targetList != null) {
            for (String cidr : targetList) {
                // 忽略空字符串和无效 CIDR
                if (cidr == null || cidr.trim().isEmpty()) {
                    continue;
                }
                try {
                    addCIDR(cidr.trim(), tempRanges);
                } catch (IllegalArgumentException ex) {
                    // 无效 CIDR 条目直接忽略，避免影响整体匹配
                }
            }

            // 按起始IP排序，用于二分查找
            tempRanges.sort(Comparator.comparingLong(CIDRRange::getStartIp));

            // 合并重叠或相邻的区间，确保区间互不重叠，便于纯二分匹配
            tempRanges = mergeRanges(tempRanges);
        }

        this.ranges = Collections.unmodifiableList(tempRanges);
    }

    /**
     * 合并重叠或相邻的 CIDR 区间
     */
    private List<CIDRRange> mergeRanges(List<CIDRRange> sortedRanges) {
        if (sortedRanges.isEmpty()) {
            return sortedRanges;
        }

        List<CIDRRange> merged = new ArrayList<>();
        CIDRRange current = sortedRanges.get(0);

        for (int i = 1; i < sortedRanges.size(); i++) {
            CIDRRange next = sortedRanges.get(i);

            if (next.getStartIp() <= current.getEndIp() + 1) {
                long newEnd = Math.max(current.getEndIp(), next.getEndIp());
                current = new CIDRRange(current.getStartIp(), newEnd);
            } else {
                merged.add(current);
                current = next;
            }
        }

        merged.add(current);
        return merged;
    }

    /**
     * 添加 CIDR 并转换为区间
     */
    private void addCIDR(String cidr, List<CIDRRange> tempRanges) {
        String[] parts = cidr.split("/");
        String ipStr = normalizeIp(parts[0]);
        if (ipStr == null) {
            throw new IllegalArgumentException("Invalid CIDR ip part: " + cidr);
        }

        int prefixLength;
        if (parts.length > 1) {
            try {
                prefixLength = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Invalid CIDR prefix: " + cidr, ex);
            }
        } else {
            // 纯 IP（无前缀）视为单个 IP，即 /32
            prefixLength = 32;
        }

        if (prefixLength < 0 || prefixLength > 32) {
            throw new IllegalArgumentException("CIDR prefix out of range: " + cidr);
        }

        long ip = ipToLong(ipStr);
        long mask = prefixLength == 0 ? 0 : 0xFFFFFFFFL << (32 - prefixLength);
        long startIp = ip & mask;
        long endIp = startIp | (~mask & 0xFFFFFFFFL);

        tempRanges.add(new CIDRRange(startIp, endIp));
    }

    /**
     * 检查 IP 地址是否允许访问
     *
     * @param ip IP 地址字符串
     * @return 是否允许访问
     */
    public boolean isAllowed(String ip) {
        boolean matches = matches(ip);
        return isAllowMode ? matches : !matches;
    }

    /**
     * 匹配IP地址是否在任何 CIDR 范围内
     */
    public boolean matches(String ip) {
        String normalized = normalizeIp(ip);
        if (normalized == null) {
            // 非法 IP 文本在这里统一视为“不匹配”
            return false;
        }

        long ipLong;
        try {
            ipLong = ipToLong(normalized);
        } catch (IllegalArgumentException ex) {
            return false;
        }

        return binarySearchMatch(ipLong);
    }
    private boolean binarySearchMatch(long ip) {
        int left = 0;
        int right = ranges.size() - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            CIDRRange range = ranges.get(mid);

            if (ip < range.getStartIp()) {
                right = mid - 1;
            } else if (ip > range.getEndIp()) {
                left = mid + 1;
            } else {
                return true;
            }
        }

        return false;
    }

    /**
     * 将 IP 地址转换为长整型
     */
    private long ipToLong(String ip) {
        Objects.requireNonNull(ip, "ip must not be null");

        String[] octets = ip.split("\\.");
        if (octets.length != 4) {
            throw new IllegalArgumentException("Invalid IPv4 format: " + ip);
        }

        long result = 0;
        for (String octet : octets) {
            int v;
            try {
                v = Integer.parseInt(octet);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Invalid IPv4 octet: " + ip, ex);
            }

            if (v < 0 || v > 255) {
                throw new IllegalArgumentException("IPv4 octet out of range: " + ip);
            }

            result = (result << 8) | v;
        }

        return result & 0xFFFFFFFFL;
    }

    /**
     * 规范化 IP 文本，例如：
     * - "localhost" -> "127.0.0.1"
     * - 去掉首尾空白
     * - 非法 / 空字符串返回 null
     */
    private String normalizeIp(String ip) {
        if (ip == null) {
            return null;
        }

        String trimmed = ip.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        if ("localhost".equalsIgnoreCase(trimmed)) {
            return "127.0.0.1";
        }

        return trimmed;
    }
}
