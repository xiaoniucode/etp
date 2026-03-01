package com.xiaoniucode.etp.client.manager;


public class MuxConnHolder {
    private static MuxConnManager manager;

    public static void set(MuxConnManager m) {
        manager = m;
    }

    public static MuxConnManager get() {
        return manager;
    }
}
