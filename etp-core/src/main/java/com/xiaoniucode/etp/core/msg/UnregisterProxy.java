package com.xiaoniucode.etp.core.msg;

public class UnregisterProxy implements Message{
    private Integer proxyId;
    public UnregisterProxy(Integer proxyId){
        this.proxyId = proxyId;
    }

    public Integer getProxyId() {
        return proxyId;
    }

    public void setProxyId(Integer proxyId) {
        this.proxyId = proxyId;
    }

    @Override
    public byte getType() {
        return Message.TYPE_UNREGISTER_PROXY;
    }
}
