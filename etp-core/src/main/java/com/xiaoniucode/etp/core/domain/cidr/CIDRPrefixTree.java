package com.xiaoniucode.etp.core.domain.cidr;

import java.util.Set;

/**
 * CIDR 前缀树
 *
 * @author xiaoniucode
 */
public class CIDRPrefixTree {

    private final CIDRNode root;

    /**
     * 构建前缀树
     *
     * @param cidrs CIDR 列表
     */
    public CIDRPrefixTree(Set<String> cidrs) {
        this.root = new CIDRNode();
        for (String cidr : cidrs) {
            insertCIDR(cidr);
        }
    }

    /**
     * 将 CIDR 插入前缀树
     *
     * @param cidr CIDR字符串 (如 43.132.115.160/32)
     */
    private void insertCIDR(String cidr) {
        String[] parts = cidr.split("/");
        String ipStr = parts[0];
        int prefixLength = parts.length > 1 ? Integer.parseInt(parts[1]) : 32;

        // 特殊处理前缀长度为0的情况 (0.0.0.0/0)
        if (prefixLength == 0) {
            root.setEnd(true);
            return;
        }

        String binaryIp = ipToBinary(ipStr);

        // 插入前缀树
        CIDRNode current = root;
        for (int i = 0; i < prefixLength; i++) {
            char bit = binaryIp.charAt(i);
            if (bit == '0') {
                if (current.getLeft() == null) {
                    current.setLeft(new CIDRNode());
                }
                current = current.getLeft();
            } else {
                if (current.getRight() == null) {
                    current.setRight(new CIDRNode());
                }
                current = current.getRight();
            }
        }
        // 标记 CIDR 结束
        current.setEnd(true);
    }

    /**
     * 匹配IP地址是否在任何 CIDR 范围内
     *
     * @param ip IP 地址字符串
     * @return 是否匹配
     */
    public boolean matches(String ip) {
        // 如果根节点是结束节点，说明有0.0.0.0/0规则，匹配所有IP
        if (root.isEnd()) {
            return true;
        }

        String binaryIp = ipToBinary(ip);
        CIDRNode current = root;

        for (int i = 0; i < binaryIp.length(); i++) {
            char bit = binaryIp.charAt(i);
            if (bit == '0') {
                current = current.getLeft();
            } else {
                current = current.getRight();
            }

            if (current == null) {
                break;
            }

            // 如果当前节点是一个CIDR的结束，则匹配成功
            if (current.isEnd()) {
                return true;
            }
        }

        return false;
    }

    /**
     * 将IP地址转换为32位二进制字符串
     *
     * @param ip IP 地址字符串
     * @return 32位二进制字符串
     */
    private String ipToBinary(String ip) {
        StringBuilder binary = new StringBuilder();
        String[] octets = ip.split("\\.");

        for (String octet : octets) {
            int value = Integer.parseInt(octet);
            String binaryOctet = String.format("%8s", Integer.toBinaryString(value)).replace(' ', '0');
            binary.append(binaryOctet);
        }

        return binary.toString();
    }
}
