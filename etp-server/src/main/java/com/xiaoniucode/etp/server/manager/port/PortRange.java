package com.xiaoniucode.etp.server.manager.port;

/**
 * 支持单个端口或端口范围分配
 */
public class PortRange {
    private int single;
    private int start;
    private int end;

    /**
     * 创建单个端口实例
     */
    public PortRange(int single) {
        this.single = single;
        this.start = 0;
        this.end = 0;
    }

    /**
     * 创建端口范围实例
     */
    public PortRange(int start, int end) {
        this.single = 0;
        this.start = start;
        this.end = end;
    }

    public int getSingle() {
        return single;
    }

    public void setSingle(int single) {
        this.single = single;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    /**
     * 是否是单个端口
     */
    public boolean isSinglePort() {
        return single > 0;
    }

    /**
     * 是否是端口范围
     */
    public boolean isPortRange() {
        return start > 0 && end >= start;
    }
}