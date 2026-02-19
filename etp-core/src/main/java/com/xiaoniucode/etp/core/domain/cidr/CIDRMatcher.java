package com.xiaoniucode.etp.core.domain.cidr;

import com.xiaoniucode.etp.core.enums.AccessControlMode;

import java.util.Set;

/**
 * CIDR IP地址匹配器
 */
public class CIDRMatcher {

    private final CIDRPrefixTree prefixTree;
    private final boolean isAllowMode;

    /**
     * 构建前缀树
     *
     * @param mode      匹配模式
     * @param allowList 白名单模式下的允许 IP 列表
     * @param denyList  黑名单模式下的拒绝 IP 列表
     */
    public CIDRMatcher(AccessControlMode mode, Set<String> allowList, Set<String> denyList) {
        this.isAllowMode = AccessControlMode.ALLOW.equals(mode);
        if (isAllowMode) {
            this.prefixTree = new CIDRPrefixTree(allowList);
        } else {
            this.prefixTree = new CIDRPrefixTree(denyList);
        }
    }

    /**
     * 检查 IP 地址是否允许访问
     *
     * @param ip IP 地址字符串
     * @return 是否允许访问
     */
    public boolean isAllowed(String ip) {
        boolean matches = prefixTree.matches(ip);
        return isAllowMode ? matches : !matches;
    }
}
