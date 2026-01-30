package com.xiaoniucode.etp.server.web.common.server.domain;

public class ServerInfo {
    private CpuInfo cpu;
    private JvmMemoryInfo jvmMem;
    private OsMemoryInfo osMem;

    public CpuInfo getCpu() {
        return cpu;
    }

    public void setCpu(CpuInfo cpu) {
        this.cpu = cpu;
    }

    public JvmMemoryInfo getJvmMem() {
        return jvmMem;
    }

    public void setJvmMem(JvmMemoryInfo jvmMem) {
        this.jvmMem = jvmMem;
    }

    public OsMemoryInfo getOsMem() {
        return osMem;
    }

    public void setOsMem(OsMemoryInfo osMem) {
        this.osMem = osMem;
    }
}
