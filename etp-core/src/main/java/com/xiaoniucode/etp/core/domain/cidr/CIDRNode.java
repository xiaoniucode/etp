package com.xiaoniucode.etp.core.domain.cidr;

/**
 * CIDR 前缀树节点
 * @author xiaoniucode
 */
public class CIDRNode {

    /**
     * 左子节点 (0)
     */
    private CIDRNode left;

    /**
     * 右子节点 (1)
     */
    private CIDRNode right;

    /**
     * 是否是一个完整 CIDR 的结束
     */
    private boolean isEnd;

    public CIDRNode getLeft() {
        return left;
    }

    public void setLeft(CIDRNode left) {
        this.left = left;
    }

    public CIDRNode getRight() {
        return right;
    }

    public void setRight(CIDRNode right) {
        this.right = right;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public void setEnd(boolean end) {
        isEnd = end;
    }
}
