package cn.xilio.vine.server.store;


import cn.xilio.vine.common.TomlUtils;


import cn.xilio.vine.core.ProtocolType;
import com.moandjiezana.toml.Toml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ProxyManager {
    private static final Logger logger = LoggerFactory.getLogger(ProxyManager.class);
    /**
     * 饿汉单例模式
     */
    private static final ProxyManager INSTANCE = new ProxyManager();
    /**
     * 默认的代理配置信息存储路径，如果用户没有指定则采用默认的。
     */
    private static final String DEFAULT_PROXY_PATH = System.getProperty("user.home") + "/vine/" + "proxy.toml";
    /**
     * 存储客户端信息，包括客户端的服务端口配置信息
     */
    private static List<Client> clients;
    /**
     * 公网端口与内网服务映射信息，内网服务包括内网的IP和PORT信息。
     */
    private static final Map<Integer, LocalServerInfo> portLocalServerMapping = new HashMap<>();

    private ProxyManager() {
    }

    public static ProxyManager getInstance() {
        return INSTANCE;
    }

    /**
     * 初始化代理配置信息
     *
     * @param proxyPath 代理配置文件路径
     */
    public static void init(String proxyPath) {
        logger.info("开始初始化客户端代理配置信息");
        clients = new ArrayList<>();
        Toml toml = TomlUtils.readToml(proxyPath);
        for (Toml client : toml.getTables("clients")) {
            String name = client.getString("name");
            Long status = client.getLong("status");
            String secretKey = client.getString("secretKey");
            //创建一个客户端
            Client c = new Client();
            c.setName(name);
            c.setStatus(status.intValue());
            c.setSecretKey(secretKey);

            List<ProxyMapping> proxyMappings = new ArrayList<>();
            List<Toml> proxies = client.getTables("proxies");
            for (Toml proxy : proxies) {
                String proxyName = proxy.getString("name");
                String type = proxy.getString("type");
                String localIP = proxy.getString("localIP");
                Long localPort = proxy.getLong("localPort");
                Long remotePort = proxy.getLong("remotePort");

                ProxyMapping proxyMapping = new ProxyMapping();
                proxyMapping.setName(proxyName);
                proxyMapping.setType(ProtocolType.getType(type));
                proxyMapping.setLocalIP(localIP);
                proxyMapping.setLocalPort(localPort.intValue());
                proxyMapping.setRemotePort(remotePort.intValue());
                proxyMappings.add(proxyMapping);
                //记录公网端口到内网服务的映射
                if (portLocalServerMapping.containsKey(remotePort.intValue())) {
                    throw new IllegalArgumentException("公网端口映射冲突，一个公网端口只能对应一个内网服务！");
                }
                portLocalServerMapping.put(remotePort.intValue(), new LocalServerInfo(localIP, localPort.intValue()));
            }
            c.setProxyMappings(proxyMappings);
            clients.add(c);
        }
        logger.info("客户端代理配置信息初始化完成");
    }

    /**
     * 获取所有客户端的公网端口
     *
     * @return 公网端口列表
     */
    public List<Integer> getAllPublicNetworkPort() {
        return new ArrayList<>(portLocalServerMapping.keySet());
    }
}
