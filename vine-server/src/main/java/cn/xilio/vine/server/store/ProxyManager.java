package cn.xilio.vine.server.store;


import cn.xilio.vine.common.TomlUtils;


import cn.xilio.vine.core.protocol.ProtocolType;
import com.moandjiezana.toml.Toml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 代理规则管理器
 *
 */
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
    private static List<ClientInfo> clients;
    private static Set<String> clientSecretKeys;
    /**
     * 公网端口与内网服务映射信息，内网服务包括内网的IP和PORT信息。
     */
    private static final Map<Integer, LocalServerInfo> portLocalServerMapping = new HashMap<>();
    /**
     * 客户端内网服务端口号列表 格式：[secretKey1:port1,secretKey2:port2]，用于用过客户端密钥获取客户端内网服务所有端口
     */
    private  static volatile Map<String, List<Integer>> clientPublicNetworkPortMapping = new HashMap<String, List<Integer>>();

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
        if (!StringUtils.hasText(proxyPath)) {
            proxyPath = DEFAULT_PROXY_PATH;
        }
        clients = new ArrayList<>();
        clientSecretKeys = new HashSet<>();
        Toml toml = TomlUtils.readToml(proxyPath);
        for (Toml client : toml.getTables("clients")) {
            String name = client.getString("name");
            Long status = client.getLong("status");
            String secretKey = client.getString("secretKey");
            if (clientPublicNetworkPortMapping.containsKey(secretKey)) {
                throw new IllegalArgumentException("客户端密钥冲突，不能存在重复的密钥！");
            }
            //创建一个客户端
            ClientInfo c = new ClientInfo();
            c.setName(name);
            c.setStatus(status.intValue());
            c.setSecretKey(secretKey);

            List<ProxyMapping> proxyMappings = new ArrayList<>();
            List<Toml> proxies = client.getTables("proxies");
            List<Integer> remotePorts = new ArrayList<>();
            clientPublicNetworkPortMapping.put(secretKey, remotePorts);
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
                remotePorts.add(remotePort.intValue());
            }
            c.setProxyMappings(proxyMappings);
            clients.add(c);
            clientSecretKeys.add(secretKey);
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

    /**
     * 根据密钥判断客户端是否存在
     *
     * @param secretKey 密钥
     */
    public boolean isClientExist(String secretKey) {
        return clientSecretKeys.contains(secretKey);
    }

    /**
     * 通过客户端密钥获取对应的内网服务对应的公网端口号列表
     *
     * @param secretKey 客户端的密钥
     * @return 所有内网服务对应的公网端口号
     */
    public List<Integer> getClientPublicNetworkPorts(String secretKey) {
        return clientPublicNetworkPortMapping.get(secretKey);
    }

    /**
     * 通过公网端口获取内网服务对应的内网服务器信息
     *
     * @param publicNetworkPort 公网端口
     * @return 内网服务器信息
     */
    public LocalServerInfo getInternalServerInfo(int publicNetworkPort) {
        return portLocalServerMapping.get(publicNetworkPort);
    }

    public   List<ClientInfo> getClients() {
        return clients;
    }
}
