package cn.xilio.etp.server.store;

import cn.xilio.etp.common.TomlUtils;
import cn.xilio.etp.server.store.dto.ClientDTO;
import cn.xilio.etp.server.store.dto.ProxyDTO;

import java.io.IOException;
import java.util.*;

/**
 * @author liuxin
 */
public final class ConfigManager {
    // private final static String configPath = "etps2.toml";
    private final static String configPath = Config.getInstance().getConfigPath();

    public static void addClient(String name, String secretKey) throws IOException {
        Map<String, Object> config = TomlUtils.readMap(configPath);
        List<Map<String, Object>> clients = getClientList(config);

        // 检查秘钥是否已存在
        for (Map<String, Object> client : clients) {
            if (secretKey.equals(client.get("secretKey"))) {
                throw new IllegalArgumentException("客户端秘钥已存在: " + secretKey);
            }
        }
        // 创建新客户端
        Map<String, Object> newClient = new LinkedHashMap<>();
        newClient.put("name", name);
        newClient.put("secretKey", secretKey);
        newClient.put("proxies", new ArrayList<Map<String, Object>>());

        clients.add(newClient);
        config.put("clients", clients);
        TomlUtils.write(config, configPath);
    }

    public static void deleteClient(String secretKey) throws IOException {
        Map<String, Object> config = TomlUtils.readMap(configPath);
        List<Map<String, Object>> clients = getClientList(config);
        boolean removed = clients.removeIf(client -> secretKey.equals(client.get("secretKey")));
        if (removed) {
            config.put("clients", clients);
            TomlUtils.write(config, configPath);
        } else {
            throw new IllegalArgumentException("未找到秘钥对应的客户端: " + secretKey);
        }
    }

    public static void addProxy(String secretKey, String name, String protocol, Long localPort, Long remotePort) throws IOException {
        Map<String, Object> config = TomlUtils.readMap(configPath);
        List<Map<String, Object>> clients = getClientList(config);
        // 查找目标客户端
        Map<String, Object> targetClient = null;
        for (Map<String, Object> client : clients) {
            if (secretKey.equals(client.get("secretKey"))) {
                targetClient = client;
                break;
            }
        }

        if (targetClient == null) {
            throw new IllegalArgumentException("未找到秘钥对应的客户端: " + secretKey);
        }

        List<Map<String, Object>> proxies = getProxiesList(targetClient);
        //检查名字是否唯一
        // 检查代理远程端口是否已存在 todo调用端口缓存检查
//         if (contains(re)){
//             throw new IllegalArgumentException("");
//         }
        // 创建新代理配置
        Map<String, Object> newProxy = new LinkedHashMap<>();
        newProxy.put("name", name);
        newProxy.put("type", protocol.toLowerCase());
        newProxy.put("localPort", localPort);
        newProxy.put("remotePort", remotePort);

        proxies.add(newProxy);
        targetClient.put("proxies", proxies);
        config.put("clients", clients);
        TomlUtils.write(config, configPath);
    }

    public static void updateProxy(String secretKey, String name, String protocol, Long localPort, Long remotePort) throws IOException {
        deleteProxy(secretKey, name);
        addProxy(secretKey, name, protocol, localPort, remotePort);
    }

    public static void deleteProxy(String secretKey, String proxyName) throws IOException {
        Map<String, Object> config = TomlUtils.readMap(configPath);
        List<Map<String, Object>> clients = getClientList(config);

        boolean removed;
        for (Map<String, Object> client : clients) {
            if (secretKey.equals(client.get("secretKey"))) {
                List<Map<String, Object>> proxies = getProxiesList(client);
                removed = proxies.removeIf(proxy -> {
                    String name = (String) proxy.get("name");
                    return name != null && name.equals(proxyName);
                });

                if (removed) {
                    client.put("proxies", proxies);
                    config.put("clients", clients);
                    // 从端口缓存中移除todo
                    TomlUtils.write(config, configPath);
                    return;
                }
                break;
            }
        }
        throw new IllegalArgumentException("未找到秘钥 " + secretKey + " 对应的名称 " + proxyName + " 的代理");
    }

    @SuppressWarnings("unchecked")
    public static List<ClientDTO> clients() {
        List<ClientDTO> res = new ArrayList<>();
        Map<String, Object> map = TomlUtils.readMap(configPath);
        Object clients = map.get("clients");
        if (Objects.isNull(clients)) {
            return res;
        }
        if (clients instanceof List) {
            List<Map<String, Object>> clientList = (List) clients;
            for (Map<String, Object> client : clientList) {
                res.add(new ClientDTO((String) client.get("name"), (String) client.get("secretKey")));
            }
        }
        return res;
    }


    public static List<ProxyDTO> proxies(String secretKey) {
        List<ProxyDTO> res = new ArrayList<>();
        Map<String, Object> map = TomlUtils.readMap(configPath);
        List<Map<String, Object>> clients = getClientList(map);
        for (Map<String, Object> client : clients) {
            if (secretKey.equals(client.get("secretKey"))) {
                List<Map<String, Object>> proxiesList = getProxiesList(client);
                for (Map<String, Object> proxy : proxiesList) {
                    String name = (String) proxy.get("name");
                    String protocol = (String) proxy.get("type");
                    Long localPort = (Long) proxy.get("localPort");
                    Long remotePort = (Long) proxy.get("remotePort");
                    res.add(new ProxyDTO(name, protocol, localPort.intValue(), remotePort == null ? null : remotePort.intValue()));
                }
                return res;
            }
        }

        return res;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> getClientList(Map<String, Object> config) {
        List<Map<String, Object>> clients = (List<Map<String, Object>>) config.get("clients");
        if (clients == null) {
            clients = new ArrayList<>();
            config.put("clients", clients);
        }
        return clients;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> getProxiesList(Map<String, Object> client) {
        List<Map<String, Object>> proxies = (List<Map<String, Object>>) client.get("proxies");
        if (proxies == null) {
            proxies = new ArrayList<>();
            client.put("proxies", proxies);
        }
        return proxies;
    }

    public static void main(String[] args) throws IOException {


        // ConfigManager.deleteProxy("123456", 323L);
        //  ConfigManager.addProxy("123456", "test3", "udp", 3365L, 323L);
        ConfigManager.updateProxy("123456", "test3", "tcp", 3365L, 323L);

    }
}
