//package cn.xilio.etp.server.store;
//
//
//import cn.xilio.etp.common.StringUtils;
//import cn.xilio.etp.common.TomlUtils;
//
//
//import cn.xilio.etp.core.protocol.ProtocolType;
//import cn.xilio.etp.server.ChannelManager;
//import com.moandjiezana.toml.Toml;
//import io.netty.util.internal.StringUtil;
//
//import java.util.*;
//import java.util.stream.Stream;
//
///**
// * @author liuxin
// */
//public class Config {
//    private static final Config INSTANCE = new Config();
//    private static String host;
//    private static Integer bindPort;
//    private static Dashboard dashboard;
//    private static List<ClientInfo> clients;
//    private static Set<String> clientSecretKeys;
//    private static final Map<Integer, Integer> portLocalServerMapping = new HashMap<>();
//    /**
//     * 客户端与对应的所有公网端口映射
//     */
//    private static final Map<String, List<Integer>> clientPublicNetworkPortMapping = new HashMap<>();
//    private static boolean tls;
//    private static KeystoreConfig keystoreConfig;
//    private static String configPath;
//
//    public record KeystoreConfig(String path, String keyPass, String storePass) {
//    }
//
//    private Config() {
//    }
//
//    public static Config getInstance() {
//        return INSTANCE;
//    }
//
//    public static void init(String path) {
//        configPath = path;
//        clients = new ArrayList<>();
//        clientSecretKeys = new HashSet<>();
//        Toml toml = TomlUtils.readToml(configPath);
//        if (toml.contains("bindPort")) {
//            bindPort = toml.getLong("bindPort").intValue();
//        }
//        if (toml.contains("host")) {
//            host = toml.getString("host");
//        }
//        //SSL密钥
//        Boolean tlsValue = toml.getBoolean("tls");
//        tls = (tlsValue != null) ? tlsValue : false;
//        if (tls) {
//            Toml keystore = toml.getTable("keystore");
//            if (keystore != null) {
//                String keyPath = keystore.getString("path");
//                String keyPass = keystore.getString("keyPass");
//                String storePass = keystore.getString("storePass");
//                if (keyPath != null && keyPass != null && storePass != null) {
//                    keystoreConfig = new KeystoreConfig(keyPath, keyPass, storePass);
//                }
//                if (keystoreConfig != null) {
//                    // 清理可能存在的旧配置
//                    System.clearProperty("server.keystore.path");
//                    System.clearProperty("server.keystore.keyPass");
//                    System.clearProperty("server.keystore.storePass");
//                    //添加到系统属性中
//                    System.setProperty("server.keystore.path", keystoreConfig.path());
//                    System.setProperty("server.keystore.keyPass", keystoreConfig.keyPass());
//                    System.setProperty("server.keystore.storePass", keystoreConfig.storePass());
//                }
//            }
//        }
//        //dashboard
//        Toml dash = toml.getTable("dashboard");
//        if (dash != null) {
//            Boolean enable = dash.getBoolean("enable") != null && dash.getBoolean("enable");
//            String addr = dash.getString("addr");
//            Integer port = dash.getLong("port") == null ? null : dash.getLong("port").intValue();
//            String username = dash.getString("username");
//            String password = dash.getString("password");
//            dashboard = new Dashboard(enable, username, password, addr, port);
//        }
//        //代理端口
//        List<Toml> readClients = toml.getTables("clients");
//        if (readClients == null) {
//            return;
//        }
//        for (Toml client : readClients) {
//            String name = client.getString("name");
//            String secretKey = client.getString("secretKey");
//            if (clientPublicNetworkPortMapping.containsKey(secretKey)) {
//                throw new IllegalArgumentException("客户端密钥冲突，不能存在重复的密钥！");
//            }
//            //创建一个客户端
//            ClientInfo clientInfo = new ClientInfo();
//            clientInfo.setName(name);
//            clientInfo.setSecretKey(secretKey);
//            List<Toml> proxies = client.getTables("proxies");
//            if (proxies != null) {
//                //代理信息配置
//                List<ProxyMapping> proxyMappings = new ArrayList<>();
//                List<Integer> remotePorts = new ArrayList<>();
//                clientPublicNetworkPortMapping.put(secretKey, remotePorts);
//                for (Toml proxy : proxies) {
//                    ProxyMapping proxyMapping = new ProxyMapping();
//                    String proxyName = proxy.getString("name");
//                    String type = proxy.getString("type");
//                    Long localPort = proxy.getLong("localPort");
//                    Long remotePort = proxy.getLong("remotePort");
//                    Long status = proxy.getLong("status");
//                    proxyMapping.setStatus(status == null ? 1 : status.intValue());
//                    if (Objects.isNull(localPort)) {
//                        throw new IllegalArgumentException("必须指定内网服务端口");
//                    }
//                    //如果没有设置名字采用内网端口作为名字
//                    proxyMapping.setName(StringUtils.hasText(proxyName) ? proxyName : String.valueOf(localPort));
//                    proxyMapping.setType(ProtocolType.getType(type));
//                    proxyMapping.setLocalPort(localPort.intValue());
//                    if (!Objects.isNull(remotePort)) {
//                        proxyMapping.setRemotePort(remotePort.intValue());
//                    }
//                    proxyMappings.add(proxyMapping);
//                    //todo 记录公网端口到内网服务的映射
//                    if (!Objects.isNull(remotePort) && portLocalServerMapping.containsKey(remotePort.intValue())) {
//                        throw new IllegalArgumentException("公网端口映射冲突，一个公网端口只能对应一个内网服务！");
//                    }
//                    //todo 需要记录 ，如果用户没有指定
//                    if (!Objects.isNull(remotePort)) {
//                        portLocalServerMapping.put(remotePort.intValue(), localPort.intValue());
//                        remotePorts.add(remotePort.intValue());
//                    }
//                }
//                clientInfo.setProxyMappings(proxyMappings);
//            }
//            clients.add(clientInfo);
//            clientSecretKeys.add(secretKey);
//        }
//    }
//
//    /**
//     * 添加代理映射
//     *
//     * @param secretKey    客户端密钥
//     * @param proxyMapping 代理映射信息
//     * @return 是否添加成功
//     */
//    public boolean addProxyMapping(String secretKey, ProxyMapping proxyMapping) {
//        List<Integer> remotePorts = clientPublicNetworkPortMapping.get(secretKey);
//        remotePorts.add(proxyMapping.getRemotePort());
//        ClientInfo clientInfo = clients.stream().filter(c -> c.getSecretKey().equals(secretKey)).findFirst().orElse(null);
//        if (clientInfo != null) {
//            clientInfo.getProxyMappings().add(proxyMapping);
//            //公网端口与内网端口建立映射
//            portLocalServerMapping.put(proxyMapping.getRemotePort(), proxyMapping.getLocalPort());
//            //将公网端口添加到客户端中
//            clientPublicNetworkPortMapping.get(secretKey).add(proxyMapping.getRemotePort());
//            //如果客户度已经启动认证
//            ChannelManager.addPortToControlChannelIfOnline(secretKey, proxyMapping.getRemotePort());
//        }
//        return false;
//    }
//
//    /**
//     * 删除代理映射
//     *
//     * @param secretKey  客户端密钥
//     * @param remotePort 公网端口
//     * @return 是否删除成功
//     */
//    public boolean deleteProxyMapping(String secretKey, Integer remotePort) {
//        List<Integer> remotePorts = clientPublicNetworkPortMapping.get(secretKey);
//        remotePorts.remove(remotePort);
//        ClientInfo clientInfo = clients.stream().filter(c -> c.getSecretKey().equals(secretKey)).findFirst().orElse(null);
//        if (clientInfo != null) {
//            clientInfo.getProxyMappings()
//                    .stream()
//                    .filter(proxyMapping -> proxyMapping.getRemotePort().equals(remotePort))
//                    .findFirst()
//                    .ifPresent(mapping -> clientInfo.getProxyMappings().remove(mapping));
//            //公网端口与内网端口建立映射
//            portLocalServerMapping.remove(remotePort);
//            //删除公网端口与已认证客户端的绑定
//            ChannelManager.removeRemotePortToControlChannel(secretKey, remotePort);
//        }
//        return true;
//    }
//
//    /**
//     * 更新代理映射信息
//     *
//     * @param secretKey    客户端密钥
//     * @param proxyMapping 需要更新的代理映射信息
//     * @return 是否更新成功
//     */
//    public boolean updateProxyMapping(String secretKey, int oldRemotePort, ProxyMapping proxyMapping) {
//       //todo 有严重bug 需要修复
//        List<Integer> remotePorts = clientPublicNetworkPortMapping.get(secretKey);
//        remotePorts.remove(Integer.valueOf(oldRemotePort));
//        remotePorts.add(proxyMapping.getRemotePort());
//        ClientInfo clientInfo = clients.stream().filter(c -> c.getSecretKey().equals(secretKey)).findFirst().orElse(null);
//        if (clientInfo != null) {
//            List<ProxyMapping> proxyMappings = clientInfo.getProxyMappings();
//            ProxyMapping old = proxyMappings.stream().filter(pm -> pm.getRemotePort().equals(oldRemotePort)).findFirst().orElse(null);
//            if (Objects.nonNull(old)) {
//                proxyMappings.remove(old);
//                proxyMappings.add(proxyMapping);
//                //公网端口与内网端口建立映射
//                portLocalServerMapping.remove(oldRemotePort);
//                portLocalServerMapping.put(proxyMapping.getRemotePort(), proxyMapping.getLocalPort());
//                //将公网端口添加到客户端中
//                clientPublicNetworkPortMapping.get(secretKey).remove(Integer.valueOf(oldRemotePort));
//                clientPublicNetworkPortMapping.get(secretKey).add(proxyMapping.getRemotePort());
//            }
//        }
//        return false;
//    }
//
//    /**
//     * 更新代理映射的状态，只有开启才能使用该代理
//     *
//     * @param secretKey  客户端密钥
//     * @param remotePort 公网端口
//     * @param status     需要更新的状态
//     * @return 是否更新成功
//     */
//    public boolean updateProxyMappingStatus(String secretKey, Integer remotePort, Integer status) {
//        ClientInfo clientInfo = clients.stream().filter(c -> c.getSecretKey().equals(secretKey)).findFirst().orElse(null);
//        if (clientInfo != null) {
//            List<ProxyMapping> proxyMappings = clientInfo.getProxyMappings();
//            if (proxyMappings != null) {
//                for (ProxyMapping proxyMapping : proxyMappings) {
//                    if (proxyMapping.getRemotePort().equals(remotePort)) {
//                        proxyMapping.setStatus(status == null ? 1 : status);
//                    }
//                }
//            }
//        }
//        return true;
//    }
//
//    /**
//     * 更新管理界面用户信息
//     *
//     * @param username 用户名
//     * @param password 密码
//     * @return 是否更新成功
//     */
//    public boolean updateDashboard(String username, String password) {
//        return true;
//    }
//
//    /**
//     * 新增客户端
//     *
//     * @param clientInfo 客户端信息
//     */
//    public boolean addClient(ClientInfo clientInfo) {
//        clients.add(clientInfo);
//        clientSecretKeys.add(clientInfo.getSecretKey());
//        clientPublicNetworkPortMapping.put(clientInfo.getSecretKey(), new ArrayList<>());
//        return true;
//    }
//
//    /**
//     * 更新客户端
//     *
//     * @param secretKey  密钥
//     * @param clientName 客户端信息
//     */
//    public boolean updateClient(String secretKey, String clientName) {
//        clients.stream().filter(c -> c.getSecretKey().equals(secretKey)).findFirst().ifPresent(c -> {
//            c.setName(clientName);
//        });
//        return true;
//    }
//
//    /**
//     * 删除客户端
//     *
//     * @param secretKey 客户端密钥
//     */
//    public boolean deleteClient(String secretKey) {
//        ClientInfo clientInfo = clients.stream().filter(c -> c.getSecretKey().equals(secretKey)).findFirst().orElse(null);
//        ChannelManager.closeControlChannelByClient(secretKey);
//        clients.remove(clientInfo);
//        clientSecretKeys.remove(secretKey);
//        return true;
//    }
//
//    /**
//     * 判断公网端口是否已经被分配
//     *
//     * @param remotePort 公网端口
//     * @return 是否被使用
//     */
//    public boolean existRemotePort(Integer remotePort) {
//        return portLocalServerMapping.containsKey(remotePort.intValue());
//    }
//
//    public List<Integer> getAllPublicNetworkPort() {
//        return new ArrayList<>(portLocalServerMapping.keySet());
//    }
//
//    /**
//     * 根据密钥判断客户端是否存在
//     *
//     * @param secretKey 密钥
//     */
//    public boolean isClientExist(String secretKey) {
//        return clientSecretKeys.contains(secretKey);
//    }
//
//    public List<Integer> getPublicNetworkPorts(String secretKey) {
//        //todo 改为从数据库获取
//        List<Integer> res = clientPublicNetworkPortMapping.get(secretKey);
//        return res != null ? res : new ArrayList<>();
//    }
//
//    public void addClientPublicNetworkPortMapping(String secretKey, Integer remotePort) {
//        List<Integer> ports = clientPublicNetworkPortMapping.get(secretKey);
//        ports.add(remotePort);
//        clientPublicNetworkPortMapping.put(secretKey, ports);
//    }
//
//    public Integer getInternalServerInfo(int remotePort) {
//        return portLocalServerMapping.get(remotePort);
//    }
//
//    public List<ClientInfo> getClients() {
//        return clients;
//    }
//
//    public Integer getBindPort() {
//        return bindPort;
//    }
//
//    public boolean isSsl() {
//        return tls;
//    }
//
//    public KeystoreConfig getKeystoreConfig() {
//        return keystoreConfig;
//    }
//
//    public Map<Integer, Integer> getPortLocalServerMapping() {
//        return portLocalServerMapping;
//    }
//
//    public String getHost() {
//        return host;
//    }
//
//    public String getConfigPath() {
//        return configPath;
//    }
//
//    public Dashboard getDashboard() {
//        return dashboard;
//    }
//}
