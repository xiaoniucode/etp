//package com.xiaoniucode.etp.client;
//
//
//import com.xiaoniucode.etp.client.helper.ProxyRespHelper;
//import com.xiaoniucode.etp.client.manager.ChannelManager;
//import com.xiaoniucode.etp.core.codec.ProtocolType;
//import com.xiaoniucode.etp.core.msg.CloseProxy;
//import com.xiaoniucode.etp.core.msg.NewProxy;
//import com.xiaoniucode.etp.core.msg.NewProxyResp;
//import io.netty.channel.Channel;
//
//import java.util.Set;
//
///**
// * 映射注册客户端
// *
// * @author liuxin
// */
//public class ProxyClient {
//    /**
//     * 注册端口映射
//     */
//    public void registerProxy(NewProxy newProxy) {
//        check(newProxy);
//        Channel controlChannel = ChannelManager.getControlChannel();
//        controlChannel.writeAndFlush(newProxy);
//    }
//
//    private void check(NewProxy newProxy) {
//
//    }
//
//    /**
//     * 注销端口映射
//     */
//    public void unregisterProxy() {
//        Channel controlChannel = ChannelManager.getControlChannel();
//        if (controlChannel != null) {
//            NewProxyResp resp = ProxyRespHelper.get();
//            if (resp != null) {
//                CloseProxy closeProxy = new CloseProxy(resp.getSessionId(), resp.getProxyId());
//                controlChannel.writeAndFlush(closeProxy);
//            }
//        }
//    }
//
//    /**
//     * NewProxy构建器
//     */
//    public static class NewProxyBuilder {
//        private final NewProxy newProxy;
//
//        private NewProxyBuilder() {
//            this.newProxy = new NewProxy();
//        }
//
//        public static NewProxyBuilder builder() {
//            return new NewProxyBuilder();
//        }
//
//        public NewProxyBuilder name(String name) {
//            newProxy.setName(name);
//            return this;
//        }
//
//        public NewProxyBuilder localIP(String localIP) {
//            newProxy.setLocalIP(localIP);
//            return this;
//        }
//
//        public NewProxyBuilder localPort(Integer localPort) {
//            newProxy.setLocalPort(localPort);
//            return this;
//        }
//
//        public NewProxyBuilder protocol(ProtocolType protocol) {
//            newProxy.setProtocol(protocol);
//            return this;
//        }
//
//        public NewProxyBuilder remotePort(Integer remotePort) {
//            newProxy.setRemotePort(remotePort);
//            return this;
//        }
//
//        public NewProxyBuilder status(Integer status) {
//            newProxy.setStatus(status);
//            return this;
//        }
//
//        public NewProxyBuilder customDomains(Set<String> customDomains) {
//            newProxy.setCustomDomains(customDomains);
//            return this;
//        }
//
//        public NewProxyBuilder subDomains(Set<String> subDomains) {
//            newProxy.setSubDomains(subDomains);
//            return this;
//        }
//
//        public NewProxyBuilder autoDomain(Boolean autoDomain) {
//            newProxy.setAutoDomain(autoDomain);
//            return this;
//        }
//
//        public NewProxy build() {
//            return newProxy;
//        }
//    }
//}
