package cn.xilio.etp.server.store;

import cn.xilio.etp.common.TomlUtils;
import cn.xilio.etp.core.protocol.ProtocolType;
import cn.xilio.etp.server.PortAllocator;
import cn.xilio.etp.server.TcpProxyServer;
import cn.xilio.etp.server.store.dto.ClientDTO;
import cn.xilio.etp.server.store.dto.ProxyDTO;
import cn.xilio.etp.server.web.framework.BizException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * @author liuxin
 */
public final class ConfigManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    private final static String CONFIG_PATH = Config.getInstance().getConfigPath();

    public static void addClient(String name, String secretKey) throws IOException {
        Map<String, Object> config = TomlUtils.readMap(CONFIG_PATH);
        List<Map<String, Object>> clients = getClientList(config);

        // 检查秘钥是否已存在
        for (Map<String, Object> client : clients) {
            if (name.equals(client.get("name"))) {
                throw new BizException("客户端名称已存在: " + name);
            }
        }
        // 创建新客户端
        Map<String, Object> newClient = new LinkedHashMap<>();
        newClient.put("name", name);
        newClient.put("secretKey", secretKey);
        newClient.put("proxies", new ArrayList<Map<String, Object>>());

        clients.add(newClient);
        config.put("clients", clients);
        ClientInfo clientInfo = new ClientInfo();
        clientInfo.setName(name);
        clientInfo.setSecretKey(secretKey);
        clientInfo.setProxyMappings(new ArrayList<>());
        //添加到配置类
        Config.getInstance().addClient(clientInfo);
        //持久化
        TomlUtils.write(config, CONFIG_PATH);
    }

    public static void deleteClient(String secretKey) throws IOException {
        Map<String, Object> config = TomlUtils.readMap(CONFIG_PATH);
        List<Map<String, Object>> clients = getClientList(config);
        boolean removed = clients.removeIf(client -> secretKey.equals(client.get("secretKey")));
        if (removed) {
            //删除客户端映射信息
            Config.getInstance().deleteClient(secretKey);
            //关闭该客户端所有运行状态的代理服务
            Config.getInstance().getPublicNetworkPorts(secretKey).forEach(remotePort -> {
                TcpProxyServer.getInstance().stopRemotePort(remotePort, true);
            });
            //持久化到文件
            config.put("clients", clients);
            TomlUtils.write(config, CONFIG_PATH);
        } else {
            throw new BizException("未找到秘钥对应的客户端: " + secretKey);
        }
    }

    public static void addProxy(String secretKey, String name, String protocol, Integer localPort, Integer remotePort, Integer status) throws IOException {
        Map<String, Object> config = TomlUtils.readMap(CONFIG_PATH);
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
            throw new BizException("未找到秘钥对应的客户端: " + secretKey);
        }

        List<Map<String, Object>> proxies = getProxiesList(targetClient);
        if (PortAllocator.getInstance().isPortAllocated(remotePort)) {
            throw new BizException("公网端口已经被使用");
        }
        // 创建新代理配置
        Map<String, Object> newProxy = new LinkedHashMap<>();
        newProxy.put("name", name);
        newProxy.put("type", protocol.toLowerCase());
        newProxy.put("localPort", localPort);
        newProxy.put("remotePort", remotePort);
        newProxy.put("status", status);

        proxies.add(newProxy);
        targetClient.put("proxies", proxies);
        config.put("clients", clients);
        //添加到内存
        ProxyMapping proxyMapping = new ProxyMapping();
        proxyMapping.setName(name);
        proxyMapping.setType(ProtocolType.getType(protocol));
        proxyMapping.setStatus(status);
        proxyMapping.setLocalPort(localPort);
        proxyMapping.setRemotePort(remotePort);
        Config.getInstance().addProxyMapping(secretKey, proxyMapping);
        //如果状态是1，需要启动代理
        if (status == 1) {
            TcpProxyServer.getInstance().startRemotePort(remotePort);
        }
        //将公网端口添加到已分配缓存
        PortAllocator.getInstance().addRemotePort(remotePort);
        //持久化到文件
        TomlUtils.write(config, CONFIG_PATH);
    }

    public static void deleteProxy(String secretKey, Integer remotePort) throws IOException {
        Map<String, Object> config = TomlUtils.readMap(CONFIG_PATH);
        List<Map<String, Object>> clients = getClientList(config);

        boolean removed;
        for (Map<String, Object> client : clients) {
            if (secretKey.equals(client.get("secretKey"))) {
                List<Map<String, Object>> proxies = getProxiesList(client);
                removed = proxies.removeIf(proxy -> {
                    Long port = (Long) proxy.get("remotePort");
                    return port != null && port.intValue() == (remotePort);
                });
                if (removed) {
                    client.put("proxies", proxies);
                    config.put("clients", clients);
                    //删除内存中的映射信息
                    Config.getInstance().deleteProxyMapping(secretKey, remotePort);
                    //停掉运行的服务并释放端口
                    TcpProxyServer.getInstance().stopRemotePort(remotePort, true);
                    //持久化到文件
                    TomlUtils.write(config, CONFIG_PATH);
                    return;
                }
                break;
            }
        }
        throw new BizException("未找到秘钥 " + secretKey + " 对应的端口 " + remotePort + " 的代理");
    }

    public static JSONArray clients() {
        JSONArray result = new JSONArray();
        Map<String, Object> config = TomlUtils.readMap(CONFIG_PATH);
        if (config == null) {
            return result;
        }
        Object clientsObj = config.get("clients");
        if (clientsObj == null || !(clientsObj instanceof List)) {
            return result;
        }
        List<?> rawList = (List<?>) clientsObj;
        for (Object item : rawList) {
            if (!(item instanceof Map)) {
                continue;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> client = (Map<String, Object>) item;

            JSONObject jsonObj = new JSONObject();

            // 必填字段
            jsonObj.put("name", Objects.toString(client.get("name"), null));
            jsonObj.put("secretKey", Objects.toString(client.get("secretKey"), null));

            // status：优先取配置中的值，没有就默认 1
            Object statusObj = client.get("status");
            int status = 1;
            if (statusObj instanceof Number) {
                status = ((Number) statusObj).intValue();
            } else if (statusObj != null) {
                try {
                    status = Integer.parseInt(statusObj.toString());
                } catch (NumberFormatException ignored) {
                    // 解析失败仍使用默认值 1
                }
            }
            jsonObj.put("status", status);

            result.put(jsonObj);
        }

        return result;
    }


    public static List<ProxyDTO> proxies() {
        List<ProxyDTO> res = new ArrayList<>();
        Map<String, Object> map = TomlUtils.readMap(CONFIG_PATH);
        List<Map<String, Object>> clients = getClientList(map);
        for (Map<String, Object> client : clients) {
            List<Map<String, Object>> proxiesList = getProxiesList(client);
            for (Map<String, Object> proxy : proxiesList) {
                String name = (String) proxy.get("name");
                String protocol = (String) proxy.get("type");
                Long localPort = (Long) proxy.get("localPort");
                Long remotePort = (Long) proxy.get("remotePort");
                Long status = (Long) proxy.get("status");
                res.add(new ProxyDTO(client.get("name").toString(),
                        client.get("secretKey").toString(),
                        name,
                        protocol.toLowerCase(Locale.ROOT),
                        localPort.intValue(),
                        remotePort == null ? null : remotePort.intValue(),
                        status == null ? 1 : status.intValue()));
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

    /**
     * 切换代理映射的状态
     *
     * @param secretKey  所属客户端
     * @param remotePort 公网端口
     * @param status     切换状态：1开启、0关闭
     */
    public static void switchProxyStatus(String secretKey, Integer remotePort, Integer status) {
        try {
            Config.getInstance().updateProxyMappingStatus(secretKey, remotePort, status);
            if (status == 1) {
                TcpProxyServer.getInstance().startRemotePort(remotePort);
            } else {
                TcpProxyServer.getInstance().stopRemotePort(remotePort, false);
            }
            //持久化
            Map<String, Object> config = TomlUtils.readMap(CONFIG_PATH);
            List<Map<String, Object>> clients = getClientList(config);
            for (Map<String, Object> client : clients) {
                if (secretKey.equals(client.get("secretKey"))) {
                    List<Map<String, Object>> proxies = getProxiesList(client);
                    for (Map<String, Object> proxy : proxies) {
                        Long port = (Long) proxy.get("remotePort");
                        if (port != null && port.intValue() == remotePort) {
                            proxy.put("status", status);
                            break;
                        }
                    }
                }
            }
            TomlUtils.write(config, CONFIG_PATH);
            logger.info("状态切换成功");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void updateClient(String secretKey, String name) {
        try {
            //更新配置
            Config.getInstance().updateClient(secretKey, name);
            //持久化
            Map<String, Object> config = TomlUtils.readMap(CONFIG_PATH);
            List<Map<String, Object>> clients = getClientList(config);
            // 检查秘钥是否已存在
            for (Map<String, Object> client : clients) {
                if (secretKey.equals(client.get("secretKey"))) {
                    client.put("name", name);
                    break;
                }
            }
            config.put("clients", clients);
            //持久化
            TomlUtils.write(config, CONFIG_PATH);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
