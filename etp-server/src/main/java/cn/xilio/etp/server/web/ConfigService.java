package cn.xilio.etp.server.web;

import cn.xilio.etp.common.StringUtils;
import cn.xilio.etp.core.protocol.ProtocolType;
import cn.xilio.etp.server.manager.ChannelManager;
import cn.xilio.etp.server.manager.PortAllocator;
import cn.xilio.etp.server.TcpProxyServer;
import cn.xilio.etp.server.config.AppConfig;
import cn.xilio.etp.server.config.ClientInfo;
import cn.xilio.etp.server.config.ProxyMapping;
import cn.xilio.etp.server.manager.RuntimeState;
import cn.xilio.etp.server.web.framework.BizException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 配置服务
 *
 * @author liuxin
 */
public final class ConfigService {
    private static Logger logger = LoggerFactory.getLogger(ConfigService.class);
    private final static ConfigStore configStore = new ConfigStore();
    private final static AppConfig config = AppConfig.get();
    private final static RuntimeState state = RuntimeState.get();

    public static void addClient(JSONObject client) {
        JSONObject existClient = configStore.getClientByName(client.getString("name"));
        if (existClient != null) {
            throw new BizException("名称不能重复");
        }
        String secretKey = UUID.randomUUID().toString().replaceAll("-", "");
        client.put("secretKey", secretKey);
        //添加到数据库
        configStore.addClient(client);
        ClientInfo clientInfo = new ClientInfo(client.getString("secretKey"));
        clientInfo.setName(client.getString("name"));
        //注册客户端
        state.registerClient(clientInfo);
    }


    /**
     * 数据统计
     */
    public static JSONObject countStats() {
        JSONObject jsonObject = new JSONObject();
        JSONArray clients = clients();
        JSONArray proxies = proxies();
        jsonObject.put("clientTotal", clients.length());
        jsonObject.put("onlineClient", ChannelManager.onlineClientCount());
        jsonObject.put("mappingTotal", proxies.length());
        jsonObject.put("startMapping", TcpProxyServer.get().runningPortCount());
        return jsonObject;
    }

    public static JSONArray clients() {
        JSONArray clients = configStore.listClients();
        for (int i = 0; i < clients.length(); i++) {
            JSONObject client = clients.getJSONObject(i);
            client.put("status", ChannelManager.clientIsOnline(client.getString("secretKey")) ? 1 : 0);
        }
        return clients;
    }

    public static JSONArray proxies() {
        return configStore.listAllProxies();
    }


    public static void addProxy(JSONObject req) {
        String remotePort = req.getString("remotePort");
        //检查公网端口是否可用
        if (StringUtils.hasText(remotePort) && !PortAllocator.getInstance().isPortAvailable(req.getInt("remotePort"))) {
            throw new BizException("公网端口不可用");
        }
        if (!StringUtils.hasText(req.getString("remotePort"))) {
            int allocatePort = PortAllocator.getInstance().allocateAvailablePort();
            req.put("remotePort", allocatePort);
        }
        configStore.addProxy(req);
        //注册端口映射
        state.registerProxy(req.getString("secretKey"), createProxyMapping(req));
        //如果客户度已经启动认证
        ChannelManager.addPortToControlChannelIfOnline(req.getString("secretKey"), req.getInt("remotePort"));
        //如果状态是1，需要启动代理服务
        if (req.getInt("status") == 1) {
            TcpProxyServer.get().startRemotePort(req.getInt("remotePort"));
        } else {
            //将公网端口添加到已分配缓存
            PortAllocator.getInstance().addRemotePort(req.getInt("remotePort"));
        }
    }

    public static void updateProxy(JSONObject req) {
        JSONObject oldProxy = configStore.getProxyById(req.getInt("id"));
        //如果没有设置公网端口，自动分配一个
        if (!StringUtils.hasText(req.getString("remotePort"))) {
            int allocatePort = PortAllocator.getInstance().allocateAvailablePort();
            req.put("remotePort", allocatePort);
        } else {
            //如果有公网端口，如果发生改变，需要判断端口是否可用
            int oldRemotePort = oldProxy.getInt("remotePort");
            if (req.getInt("remotePort") != oldRemotePort) {
                if (!PortAllocator.getInstance().isPortAvailable(req.getInt("remotePort"))) {
                    throw new BizException("公网端口不可用");
                }
            }
        }
        configStore.updateProxy(req);
        //添加到Config内存
        JSONObject proxy = configStore.getProxyById(req.getInt("id"));
        //删除已经注册的映射
        state.removeProxy(req.getString("secretKey"), proxy.getInt("remotePort"));
        //重新注册更新后的映射
        state.registerProxy(req.getString("secretKey"), createProxyMapping(req));
        //如果状态是1，需要启动代理服务
        if (req.getInt("status") == 1) {
            TcpProxyServer.get().startRemotePort(req.getInt("remotePort"));
        } else {
            //停止对应端口的代理映射服务
            TcpProxyServer.get().stopRemotePort(req.getInt("remotePort"), false);
        }
    }

    private static ProxyMapping createProxyMapping(JSONObject req) {
        ProxyMapping proxyMapping = new ProxyMapping(ProtocolType.getType(req.getString("type")),req.getInt("localPort"),req.getInt("remotePort"));
        proxyMapping.setProxyId(req.getInt("clientId"));
        proxyMapping.setName(req.getString("name"));
        proxyMapping.setStatus(req.getInt("status"));
        return proxyMapping;
    }

    /**
     * 切换端口映射状态
     */
    public static void switchProxyStatus(JSONObject req) {
        JSONObject proxy = configStore.getProxyById(req.getInt("id"));
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
        configStore.updateProxy(proxy);
    }

    public static void deleteProxy(JSONObject req) {
        int id = req.getInt("id");
        JSONObject proxy = configStore.getProxyById(id);
        String secretKey = req.getString("secretKey");
        int remotePort = proxy.getInt("remotePort");
        //删除注册的端口映射
        state.removeProxy(secretKey, remotePort);
        //删除公网端口与已认证客户端的绑定
        ChannelManager.removeRemotePortToControlChannel(secretKey, remotePort);
        //停掉运行的服务并释放端口
        TcpProxyServer.get().stopRemotePort(remotePort, true);
        configStore.deleteProxy(id);
    }

    public static void updateClient(JSONObject req) {
        JSONObject client = configStore.getClientById(req.getInt("id"));
        state.updateClientName(client.getString("secretKey"), req.getString("name"));
        configStore.updateClient(req.getInt("id"), req.getString("name"));
    }

    public static void deleteClient(JSONObject req) {
        JSONObject client = configStore.getClientById(req.getInt("id"));
        String secretKey = client.getString("secretKey");

        state.removeClient(secretKey);
        ChannelManager.closeControlChannelByClient(secretKey);

        //关闭该客户端所有运行状态的代理服务
        state.getClientRemotePorts(secretKey).forEach(remotePort -> {
            TcpProxyServer.get().stopRemotePort(remotePort, true);
        });
        configStore.deleteClient(client.getInt("id"));
        //删除客户端所有的代理映射
        configStore.deleteProxiesByClient(client.getInt("id"));
    }

    /**
     * 将在线的客户端踢下线
     */
    public static void kickoutClient(JSONObject req) {
        String secretKey = req.getString("secretKey");
        //关闭客户端隧道，同时关闭所有连接
        ChannelManager.closeControlChannelByClient(secretKey);
        //todo 需要优化，踢掉前需要发送消息通知客户端，避免断线重连
    }

    public static JSONObject getClient(JSONObject req) {
        return configStore.getClientById(req.getInt("id"));
    }

    public static JSONObject getProxy(JSONObject req) {
        return configStore.getProxyById(req.getInt("id"));
    }
}
