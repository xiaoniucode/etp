package com.xiaoniucode.etp.server.config;

/**
 * 端口范围配置
 *
 * @author liuxin
 */
public class PortRange {
    private int start ;
    private int end ;

    public PortRange() {
    }

    public PortRange(int start, int end) {
        this.start = start;
        this.end = end;
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
}
