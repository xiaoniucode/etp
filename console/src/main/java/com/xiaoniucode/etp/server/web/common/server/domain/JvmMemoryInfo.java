package com.xiaoniucode.etp.server.web.common.server.domain;

public class JvmMemoryInfo {
    private String total;
    private String used;
    private Object usage;

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getUsed() {
        return used;
    }

    public void setUsed(String used) {
        this.used = used;
    }

    public Object getUsage() {
        return usage;
    }

    public void setUsage(Object usage) {
        this.usage = usage;
    }
}
