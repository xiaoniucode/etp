package com.xiaoniucode.etp.server.web.serivce;

import com.xiaoniucode.etp.common.StringUtils;
import com.xiaoniucode.etp.core.codec.ProtocolType;
import com.xiaoniucode.etp.server.GlobalIdGenerator;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.config.ProxyMapping;
import com.xiaoniucode.etp.server.manager.ChannelManager;
import com.xiaoniucode.etp.server.manager.PortAllocator;
import com.xiaoniucode.etp.server.manager.RuntimeStateManager;
import com.xiaoniucode.etp.server.proxy.TcpProxyServer;
import com.xiaoniucode.etp.server.web.core.orm.transaction.JdbcTransactionTemplate;
import com.xiaoniucode.etp.server.web.core.server.BizException;
import com.xiaoniucode.etp.server.web.dao.DaoFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

public class ProxyService {
    private final AppConfig config = AppConfig.get();
    private final RuntimeStateManager state = RuntimeStateManager.get();
    private final JdbcTransactionTemplate TX = new JdbcTransactionTemplate();

    public JSONObject getProxy(JSONObject req) {
        JSONObject res = DaoFactory.INSTANCE.getProxyDao().getById(req.getInt("id"));
        String type = res.getString("type");
        if (ProtocolType.getType(type) == ProtocolType.HTTP) {
            String domains = res.getString("domains");
            if (StringUtils.hasText(domains)) {
                res.put("domains", String.join("\n", domains.split(",")));
            }
        }
        return res;
    }

    public void deleteAllAutoRegisterProxy() {
        DaoFactory.INSTANCE.getProxyDao().deleteAllAutoRegisterProxy();
    }

    public JSONArray proxies(String type) {
        return DaoFactory.INSTANCE.getProxyDao().listAllProxies(type);
    }

    public JSONObject addTcpProxy(JSONObject req) {
        int remotePort = req.getInt("remotePort");
        if (remotePort != -1 && !PortAllocator.get().isPortAvailable(remotePort)) {
            throw new BizException("映射注册失败，公网端口: " + remotePort + "不可用！");
        }
        String secretKey = req.getString("secretKey");
        if (state.isPortOccupied(remotePort)) {
            throw new BizException("公网端口:" + remotePort + "已被占用");
        }
        //-1表示用户没有自定义端口
        if (remotePort == -1) {
            int allocatePort = PortAllocator.get().allocateAvailablePort();
            req.put("remotePort", allocatePort);
        }
        if (!StringUtils.hasText(req.getString("name"))) {
            req.put("name", req.getInt("remotePort"));
        }
        req.put("type", ProtocolType.TCP.name().toLowerCase(Locale.ROOT));
        JSONObject res = new JSONObject();
        int remotePortInt = req.getInt("remotePort");
        //注册端口映射
        state.registerProxy(secretKey, createProxyMapping(req));
        //如果客户度已经启动认证
        ChannelManager.addPortToControlChannelIfOnline(secretKey, remotePortInt);
        //如果状态是1，需要启动代理服务
        if (req.getInt("status") == 1) {
            TcpProxyServer.get().startRemotePort(remotePortInt);
        } else {
            //将公网端口添加到已分配缓存
            PortAllocator.get().addRemotePort(remotePortInt);
        }
        //最后执行持久化
        int proxyId;
        if (config.getDashboard().getEnable()) {
            proxyId = DaoFactory.INSTANCE.getProxyDao().insert(req);
        } else {
            //纯TOML模式
            proxyId = GlobalIdGenerator.nextId();
        }
        res.put("proxyId", proxyId);
        res.put("remotePort", remotePortInt);
        return res;
    }

    public void updateTcpProxy(JSONObject req) {
        TX.execute(() -> {
            String newRemotePortString = req.getString("remotePort");
            int newRemotePortInt;

            JSONObject oldProxy = DaoFactory.INSTANCE.getProxyDao().getById(req.getInt("id"));
            int oldRemotePort = oldProxy.getInt("remotePort");
            //如果没有设置公网端口，自动分配一个
            boolean remotePortChanged = false;
            if (!StringUtils.hasText(newRemotePortString)) {
                int allocatePort = PortAllocator.get().allocateAvailablePort();
                req.put("remotePort", allocatePort);
                newRemotePortInt = allocatePort;
                remotePortChanged = true;
            } else {
                newRemotePortInt = req.getInt("remotePort");
                //如果有公网端口，如果发生改变，需要判断端口是否可用
                if (newRemotePortInt != oldRemotePort) {
                    remotePortChanged = true;
                    if (state.isPortOccupied(newRemotePortInt)) {
                        throw new BizException("公网端口已被占用");
                    }
                    if (!PortAllocator.get().isPortAvailable(newRemotePortInt)) {
                        throw new BizException("公网端口无效");
                    }
                }
            }
            req.put("type", ProtocolType.TCP.name().toLowerCase(Locale.ROOT));
            //检查名称
            String oldName = oldProxy.getString("name");
            String newName = req.getString("name");
            String secretKey = req.getString("secretKey");
            if (!oldName.equals(newName) && (DaoFactory.INSTANCE.getProxyDao().getProxy(req.getInt("clientId"), newName) != null)) {
                throw new BizException("该客户端存在同名映射");
            }
            DaoFactory.INSTANCE.getProxyDao().update(req);
            //删除已经注册的映射
            state.removeProxy(secretKey, oldRemotePort);
            //重新注册更新后的映射
            state.registerProxy(secretKey, createProxyMapping(req));
            //如果客户度已经启动认证
            ChannelManager.addPortToControlChannelIfOnline(secretKey, newRemotePortInt);
            //公网端口发生更新，需要停掉之前的服务连接
            if (remotePortChanged) {
                TcpProxyServer.get().stopRemotePort(oldRemotePort, true);
            }
            //如果状态是1，需要启动代理服务
            if (req.getInt("status") == 1) {
                TcpProxyServer.get().startRemotePort(newRemotePortInt);
            } else {
                //停止对应端口的代理映射服务
                TcpProxyServer.get().stopRemotePort(newRemotePortInt, false);
            }
            return null;
        });
    }

    public JSONObject addHttpProxy(JSONObject req) {
        return TX.execute(() -> {
            // 检查是否存在
            req.put("type", ProtocolType.HTTP.name().toLowerCase(Locale.ROOT));
            int proxyId = DaoFactory.INSTANCE.getProxyDao().insert(req);
            JSONObject res = new JSONObject();
            res.put("proxyId", proxyId);
            return res;
        });
    }

    private ProxyMapping createProxyMapping(JSONObject req) {
        ProxyMapping proxyMapping = new ProxyMapping(ProtocolType.getType(req.getString("type")),
                req.getInt("localPort"),
                req.getInt("remotePort"));
        proxyMapping.setProxyId(req.getInt("clientId"));
        proxyMapping.setName(req.getString("name"));
        proxyMapping.setStatus(req.getInt("status"));
        return proxyMapping;
    }

    /**
     * 切换端口映射状态
     */
    public void switchProxyStatus(JSONObject req) {
        TX.execute(() -> {
            JSONObject proxy = DaoFactory.INSTANCE.getProxyDao().getById(req.getInt("id"));
            int status = proxy.getInt("status");
            String secretKey = req.getString("secretKey");
            int remotePort = proxy.getInt("remotePort");
            int updateStatus = status == 1 ? 0 : 1;
            state.updateProxyStatus(secretKey, remotePort, updateStatus);
            if (updateStatus == 1) {
                TcpProxyServer.get().startRemotePort(remotePort);
            } else {
                TcpProxyServer.get().stopRemotePort(remotePort, false);
            }
            //持久化数据库
            proxy.put("status", updateStatus);
            DaoFactory.INSTANCE.getProxyDao().update(proxy);
            return null;
        });
    }

    public void deleteProxy(JSONObject req) {
        TX.execute(() -> {
            int id = req.getInt("id");
            String secretKey = req.getString("secretKey");
            JSONObject proxy = DaoFactory.INSTANCE.getProxyDao().getById(id);
            int remotePort = proxy.getInt("remotePort");
            //删除注册的端口映射
            state.removeProxy(secretKey, remotePort);
            //删除公网端口与已认证客户端的绑定
            ChannelManager.removeRemotePortToControlChannel(remotePort);
            //停掉连接的服务并释放端口
            TcpProxyServer.get().stopRemotePort(remotePort, true);
            DaoFactory.INSTANCE.getProxyDao().deleteById(id);
            return null;
        });
    }

    public void deleteProxiesByClient(int clientId) {
        DaoFactory.INSTANCE.getProxyDao().deleteProxiesByClient(clientId);
    }
}
