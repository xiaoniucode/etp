package cn.xilio.etp.server.web;

import cn.xilio.etp.server.store.ClientInfo;
import cn.xilio.etp.server.store.Config;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

/**
 * @author liuxin
 */
public final class ConfigService {
    private final static ConfigStore CONFIG_STORE = new ConfigStore();

    public static void addClient(JSONObject client) {
        String secretKey = UUID.randomUUID().toString().replaceAll("-", "");
        client.put("secretKey", secretKey);
        //添加到数据库
        CONFIG_STORE.addClient(client);
        ClientInfo clientInfo = new ClientInfo();
        clientInfo.setName(client.getString("name"));
        clientInfo.setSecretKey(client.getString("secretKey"));
        clientInfo.setProxyMappings(new ArrayList<>());
        //添加到配置
        Config.getInstance().addClient(clientInfo);
    }
}
