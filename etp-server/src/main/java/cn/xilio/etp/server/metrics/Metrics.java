package cn.xilio.etp.server.metrics;

import java.io.Serial;
import java.io.Serializable;

/**
 * 指标
 * @author liuxin
 */
public class Metrics implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private int remotePort;
    private long readMsgs;
    private long writeMsgs;
    private long readBytes;
    private long writeBytes;
    private int channels;
    private String time;

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public long getReadMsgs() {
        return readMsgs;
    }

    public void setReadMsgs(long readMsgs) {
        this.readMsgs = readMsgs;
    }

    public long getWriteMsgs() {
        return writeMsgs;
    }

    public void setWriteMsgs(long writeMsgs) {
        this.writeMsgs = writeMsgs;
    }

    public long getReadBytes() {
        return readBytes;
    }

    public void setReadBytes(long readBytes) {
        this.readBytes = readBytes;
    }

    public long getWriteBytes() {
        return writeBytes;
    }

    public void setWriteBytes(long writeBytes) {
        this.writeBytes = writeBytes;
    }

    public int getChannels() {
        return channels;
    }

    public void setChannels(int channels) {
        this.channels = channels;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
