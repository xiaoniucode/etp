package cn.xilio.etp.server.store;


import cn.xilio.etp.common.TomlUtils;


import cn.xilio.etp.core.protocol.ProtocolType;
import com.moandjiezana.toml.Toml;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

import java.util.*;

/**
 * 代理规则管理器
 */
public class Config {
    /**
     * 饿汉单例模式
     */
    private static final Config INSTANCE = new Config();
    /**
     * 默认的代理配置信息存储路径，如果用户没有指定则采用默认的。
     */
    private static final String DEFAULT_PROXY_PATH = System.getProperty("user.home") + "/etp/" + "etps.toml";

    private static Integer bindPort;
    /**
     * 存储客户端信息，包括客户端的服务端口配置信息
     */
    private static List<ClientInfo> clients;
    private static Set<String> clientSecretKeys;
    /**
     * 公网端口与内网服务映射信息，内网服务包括内网的IP和PORT信息。
     */
    private static volatile Map<Integer, Integer> portLocalServerMapping = new HashMap<>();
    /**
     * 客户端内网服务端口号列表 格式：[secretKey1:port1,secretKey2:port2]，用于用过客户端密钥获取客户端内网服务所有端口
     */
    private static volatile Map<String, List<Integer>> clientPublicNetworkPortMapping = new HashMap<String, List<Integer>>();
    private static boolean ssl;
    /**
     * SSL密钥配置
     */
    private static KeystoreConfig keystoreConfig;

    public static final class KeystoreConfig {
        private final String path;
        private final String keyPass;
        private final String storePass;

        public KeystoreConfig(String path, String keyPass, String storePass) {
            this.path = path;
            this.keyPass = keyPass;
            this.storePass = storePass;
        }

        public String getPath() {
            return path;
        }

        public String getKeyPass() {
            return keyPass;
        }

        public String getStorePass() {
            return storePass;
        }
    }

    private Config() {
    }

    public static Config getInstance() {
        return INSTANCE;
    }

    /**
     * 初始化代理配置信息
     *
     * @param proxyPath 代理配置文件路径
     */
    public static void init(String proxyPath) {
        if (proxyPath == null) {
            proxyPath = DEFAULT_PROXY_PATH;
        }
        clients = new ArrayList<>();
        clientSecretKeys = new HashSet<>();
        Toml toml = TomlUtils.readToml(proxyPath);
        if (toml.contains("bindPort")) {
            bindPort = toml.getLong("bindPort").intValue();
        }
        for (Toml client : toml.getTables("clients")) {
            String name = client.getString("name");
            String secretKey = client.getString("secretKey");
            if (clientPublicNetworkPortMapping.containsKey(secretKey)) {
                throw new IllegalArgumentException("客户端密钥冲突，不能存在重复的密钥！");
            }
            //创建一个客户端
            ClientInfo c = new ClientInfo();
            c.setName(name);
            c.setSecretKey(secretKey);

            //SSL密钥
            Boolean sslValue = toml.getBoolean("ssl");
            ssl = (sslValue != null) ? sslValue : false;
            if (ssl) {
                Toml keystore = toml.getTable("keystore");
                if (keystore != null) {
                    String path = keystore.getString("path");
                    String keyPass = keystore.getString("keyPass");
                    String storePass = keystore.getString("storePass");
                    if (path != null && keyPass != null && storePass != null) {
                        keystoreConfig = new KeystoreConfig(path, keyPass, storePass);
                    }
                    if (keystoreConfig != null) {
                        // 清理可能存在的旧配置
                        System.clearProperty("server.keystore.path");
                        System.clearProperty("server.keystore.keyPass");
                        System.clearProperty("server.keystore.storePass");
                        //添加到系统属性中
                        System.setProperty("server.keystore.path", keystoreConfig.getPath());
                        System.setProperty("server.keystore.keyPass", keystoreConfig.getKeyPass());
                        System.setProperty("server.keystore.storePass", keystoreConfig.getStorePass());
                    }

                }
            }

            List<Toml> proxies = client.getTables("proxies");
            if (proxies != null) {
                //代理信息配置
                List<ProxyMapping> proxyMappings = new ArrayList<>();
                List<Integer> remotePorts = new ArrayList<>();
                clientPublicNetworkPortMapping.put(secretKey, remotePorts);
                for (Toml proxy : proxies) {
                    ProxyMapping proxyMapping = new ProxyMapping();
                    String proxyName = proxy.getString("name");
                    String type = proxy.getString("type");
                    Long localPort = proxy.getLong("localPort");
                    Long remotePort = proxy.getLong("remotePort");

                    if (Objects.isNull(localPort)) {
                        throw new IllegalArgumentException("必须指定内网服务端口");
                    }
                    if (StringUtil.isNullOrEmpty(proxyName)) {
                        //如果没有设置名字采用内网端口号标识
                        proxyMapping.setName(String.valueOf(localPort));
                    }
                    proxyMapping.setName(proxyName);
                    proxyMapping.setType(ProtocolType.getType(type));
                    proxyMapping.setLocalPort(localPort.intValue());
                    if (!Objects.isNull(remotePort)) {
                        proxyMapping.setRemotePort(remotePort.intValue());
                    }
                    proxyMappings.add(proxyMapping);
                    //todo 记录公网端口到内网服务的映射
                    if (!Objects.isNull(remotePort) && portLocalServerMapping.containsKey(remotePort.intValue())) {
                        throw new IllegalArgumentException("公网端口映射冲突，一个公网端口只能对应一个内网服务！");
                    }
                    //todo 需要记录 ，如果用户没有指定
                    if (!Objects.isNull(remotePort)) {
                        portLocalServerMapping.put(remotePort.intValue(), localPort.intValue());
                        remotePorts.add(remotePort.intValue());
                    }
                }
                c.setProxyMappings(proxyMappings);
            }
            clients.add(c);
            clientSecretKeys.add(secretKey);
        }
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
        List<Integer> res = clientPublicNetworkPortMapping.get(secretKey);
        return res != null ? res : new ArrayList<>();
    }

    public  void addClientPublicNetworkPortMapping(String secretKey,Integer remotePort) {
        List<Integer> ports = clientPublicNetworkPortMapping.get(secretKey);
        ports.add(remotePort);
        clientPublicNetworkPortMapping.put(secretKey, ports);
    }

    /**
     * 通过公网端口获取内网服务对应的内网服务器信息
     *
     * @param publicNetworkPort 公网端口
     * @return 内网服务器信息
     */
    public Integer getInternalServerInfo(int publicNetworkPort) {
        return portLocalServerMapping.get(publicNetworkPort);
    }

    public List<ClientInfo> getClients() {
        return clients;
    }

    public Integer getBindPort() {
        return bindPort;
    }

    public boolean isSsl() {
        return ssl;
    }

    public static KeystoreConfig getKeystoreConfig() {
        return keystoreConfig;
    }

    public  Map<Integer, Integer> getPortLocalServerMapping() {
        return portLocalServerMapping;
    }
}
