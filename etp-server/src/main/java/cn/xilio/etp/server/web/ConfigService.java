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
}
