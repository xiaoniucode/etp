package com.xiaoniucode.etp.server.web.common.server.domain;

public class CpuInfo {
    private int total;
    private Object used;
    private Object usage;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public Object getUsed() {
        return used;
    }

    public void setUsed(Object used) {
        this.used = used;
    }

    public Object getUsage() {
        return usage;
    }

    public void setUsage(Object usage) {
        this.usage = usage;
    }
}
