package com.xiaoniucode.etp.core.domain.cidr;

import com.xiaoniucode.etp.core.enums.AccessControlMode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * 基于区间+二分的CIDR IP地址匹配器
 */
public class CIDRMatcher {

    /**
     * -- GETTER --
     *  获取区间列表（用于测试）
     */
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
                addCIDR(cidr, tempRanges);
            }

            // 按起始IP排序，用于二分查找
            tempRanges.sort(Comparator.comparingLong(CIDRRange::getStartIp));
        }

        this.ranges = Collections.unmodifiableList(tempRanges);
    }

    /**
     * 添加 CIDR 并转换为区间
     */
    private void addCIDR(String cidr, List<CIDRRange> tempRanges) {
        String[] parts = cidr.split("/");
        String ipStr = parts[0];
        int prefixLength = parts.length > 1 ? Integer.parseInt(parts[1]) : 32;

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
        long ipLong = ipToLong(ip);
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

        // 检查所有可能包含该IP的区间（起始IP <= ip的区间）
        for (int i = left - 1; i >= 0; i--) {
            CIDRRange range = ranges.get(i);
            if (range.getStartIp() <= ip && range.getEndIp() >= ip) {
                return true;
            }
        }

        return false;
    }

    /**
     * 将 IP 地址转换为长整型
     */
    private long ipToLong(String ip) {
        String[] octets = ip.split("\\.");
        long result = 0;
        for (int i = 0; i < 4; i++) {
            result = (result << 8) | Integer.parseInt(octets[i]);
        }
        return result & 0xFFFFFFFFL;
    }
}
