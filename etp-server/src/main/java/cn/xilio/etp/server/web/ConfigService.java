package cn.xilio.etp.server.web;

import cn.xilio.etp.core.protocol.ProtocolType;
import cn.xilio.etp.server.ChannelManager;
import cn.xilio.etp.server.PortAllocator;
import cn.xilio.etp.server.TcpProxyServer;
import cn.xilio.etp.server.store.ClientInfo;
import cn.xilio.etp.server.store.Config;
import cn.xilio.etp.server.store.ProxyMapping;
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

    public static void addClient(JSONObject client) {
        JSONObject existClient = configStore.getClientByName(client.getString("name"));
        if (existClient != null) {
            throw new BizException("名称不能重复");
        }
        String secretKey = UUID.randomUUID().toString().replaceAll("-", "");
        client.put("secretKey", secretKey);
        //添加到数据库
        configStore.addClient(client);
        ClientInfo clientInfo = new ClientInfo();
        clientInfo.setName(client.getString("name"));
        clientInfo.setSecretKey(client.getString("secretKey"));
        clientInfo.setProxyMappings(new ArrayList<>());
        //添加到配置
        Config.getInstance().addClient(clientInfo);
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
        jsonObject.put("startMapping", TcpProxyServer.getInstance().runningPortCount());
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

    public static void saveProxy(JSONObject req, boolean update) {
        //检查公网端口是否被占用
        if (!PortAllocator.getInstance().isPortAvailable(req.getInt("remotePort"))) {
            throw new BizException("公网端口不可用");
        }
        //保存到数据库
        if (update) {
            configStore.updateProxy(req);
        } else {
            configStore.addProxy(req);
        }
        //添加到Config内存
        ProxyMapping proxyMapping = new ProxyMapping();
        proxyMapping.setName(req.getString("name"));
        proxyMapping.setType(ProtocolType.getType(req.getString("type")));
        proxyMapping.setStatus(req.getInt("status"));
        proxyMapping.setLocalPort(req.getInt("localPort"));
        proxyMapping.setRemotePort(req.getInt("remotePort"));
        if (update) {
            JSONObject proxy = configStore.getProxy(req.getInt("id"));
            Config.getInstance().updateProxyMapping(req.getString("secretKey"), proxy.getInt("remotePort"), proxyMapping);
        } else {
            Config.getInstance().addProxyMapping(req.getString("secretKey"), proxyMapping);
        }
        //如果状态是1，需要启动代理服务
        if (req.getInt("status") == 1) {
            TcpProxyServer.getInstance().startRemotePort(req.getInt("remotePort"));
        } else {
            //将公网端口添加到已分配缓存
            PortAllocator.getInstance().addRemotePort(req.getInt("remotePort"));
        }
    }


    public static void switchProxyStatus(JSONObject req) {
        JSONObject proxy = configStore.getProxy(req.getInt("id"));
        int status = proxy.getInt("status");
        String secretKey = req.getString("secretKey");
        int remotePort = proxy.getInt("remotePort");
        int updateStatus = status == 1 ? 0 : 1;
        Config.getInstance().updateProxyMappingStatus(secretKey, remotePort, updateStatus);
        if (status == 1) {
            TcpProxyServer.getInstance().startRemotePort(remotePort);
        } else {
            TcpProxyServer.getInstance().stopRemotePort(remotePort, false);
        }
        //持久化数据库
        proxy.putOpt("status", updateStatus);
        configStore.updateProxy(proxy);
    }

    public static void deleteProxy(JSONObject req) {
        int id = req.getInt("id");
        JSONObject proxy = configStore.getProxy(id);
        String secretKey = req.getString("secretKey");
        int remotePort = proxy.getInt("remotePort");
        //删除内存中的映射信息
        Config.getInstance().deleteProxyMapping(secretKey, remotePort);
        //停掉运行的服务并释放端口
        TcpProxyServer.getInstance().stopRemotePort(remotePort, true);
        configStore.deleteProxy(id);
    }

    public static void updateClient(JSONObject req) {
        JSONObject client = configStore.getClientById(req.getInt("id"));
        Config.getInstance().updateClient(client.getString("secretKey"), req.getString("name"));
        configStore.updateClient(req.getInt("id"), req.getString("name"));
    }

    public static void deleteClient(JSONObject req) {
        JSONObject client = configStore.getClientById(req.getInt("id"));
        String secretKey = client.getString("secretKey");
        //删除客户端映射信息
        Config.getInstance().deleteClient(secretKey);
        //关闭该客户端所有运行状态的代理服务
        Config.getInstance().getPublicNetworkPorts(secretKey).forEach(remotePort -> {
            TcpProxyServer.getInstance().stopRemotePort(remotePort, true);
        });
        configStore.deleteClient(client.getInt("id"));
        //删除客户端所有的代理映射
        configStore.deleteProxiesByClient(client.getInt("id"));
    }

    /**
     * todo 将在线的客户端踢下线
     */
    public static void kickoutClient(JSONObject req) {
        String secretKey = req.getString("secretKey");
        //关闭客户端隧道，同时关闭所有连接
    }

    public static JSONObject getClient(JSONObject req) {
        return configStore.getClientById(req.getInt("id"));
    }
}
