package cn.xilio.etp.server.web;

import cn.xilio.etp.server.ChannelManager;
import cn.xilio.etp.server.TcpProxyServer;
import cn.xilio.etp.server.store.ClientInfo;
import cn.xilio.etp.server.store.Config;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 配置服务
 *
 * @author liuxin
 */
public final class ConfigService {
    private static Logger logger = LoggerFactory.getLogger(ConfigService.class);
    private final static ConfigStore configStore = new ConfigStore();

    public static void addClient(JSONObject client) {
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
        //同步到Toml文件
        CompletableFuture.runAsync(() -> {
            try {

            } catch (Exception e) {
                logger.error("TOML 同步失败", e);
            }
        });
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

    public static void addProxy(JSONObject jsonObject) {

        //检查端口是否合法

        //检查公网端口是否被占用

        //如果状态是1，需要启动代理服务

        //保存到数据库

        //持久化到Toml
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
        //同步到Toml
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
}
