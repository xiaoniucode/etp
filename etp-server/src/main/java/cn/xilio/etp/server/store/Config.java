package cn.xilio.etp.server.store;


import cn.xilio.etp.common.TomlUtils;


import cn.xilio.etp.core.protocol.ProtocolType;
import com.moandjiezana.toml.Toml;
import io.netty.util.internal.StringUtil;

import java.util.*;

/**
 * @author liuxin
 */
public class Config {
    private static final Config INSTANCE = new Config();
    private static String host;
    private static Integer bindPort;
    private static List<ClientInfo> clients;
    private static Set<String> clientSecretKeys;
    private static volatile Map<Integer, Integer> portLocalServerMapping = new HashMap<>();
    private static volatile Map<String, List<Integer>> clientPublicNetworkPortMapping = new HashMap<>();
    private static boolean ssl;
    private static KeystoreConfig keystoreConfig;
    private static String configPath;

    public record KeystoreConfig(String path, String keyPass, String storePass) {
    }

    private Config() {
    }

    public static Config getInstance() {
        return INSTANCE;
    }

    public static void init(String path) {
        configPath = path;
        clients = new ArrayList<>();
        clientSecretKeys = new HashSet<>();
        Toml toml = TomlUtils.readToml(configPath);
        if (toml.contains("bindPort")) {
            bindPort = toml.getLong("bindPort").intValue();
        }
        if (toml.contains("host")) {
            host = toml.getString("host");
        }
        //SSL密钥
        Boolean sslValue = toml.getBoolean("ssl");
        ssl = (sslValue != null) ? sslValue : false;
        if (ssl) {
            Toml keystore = toml.getTable("keystore");
            if (keystore != null) {
                String keyPath = keystore.getString("path");
                String keyPass = keystore.getString("keyPass");
                String storePass = keystore.getString("storePass");
                if (keyPath != null && keyPass != null && storePass != null) {
                    keystoreConfig = new KeystoreConfig(keyPath, keyPass, storePass);
                }
                if (keystoreConfig != null) {
                    // 清理可能存在的旧配置
                    System.clearProperty("server.keystore.path");
                    System.clearProperty("server.keystore.keyPass");
                    System.clearProperty("server.keystore.storePass");
                    //添加到系统属性中
                    System.setProperty("server.keystore.path", keystoreConfig.path());
                    System.setProperty("server.keystore.keyPass", keystoreConfig.keyPass());
                    System.setProperty("server.keystore.storePass", keystoreConfig.storePass());
                }
            }
        }
        //代理端口
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

    public List<Integer> getPublicNetworkPorts(String secretKey) {
        List<Integer> res = clientPublicNetworkPortMapping.get(secretKey);
        return res != null ? res : new ArrayList<>();
    }

    public void addClientPublicNetworkPortMapping(String secretKey, Integer remotePort) {
        List<Integer> ports = clientPublicNetworkPortMapping.get(secretKey);
        ports.add(remotePort);
        clientPublicNetworkPortMapping.put(secretKey, ports);
    }

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

    public KeystoreConfig getKeystoreConfig() {
        return keystoreConfig;
    }

    public Map<Integer, Integer> getPortLocalServerMapping() {
        return portLocalServerMapping;
    }

    public String getHost() {
        return host;
    }

    public  String getConfigPath() {
        return configPath;
    }
}
