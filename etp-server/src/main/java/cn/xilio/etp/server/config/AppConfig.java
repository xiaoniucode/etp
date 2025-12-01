package cn.xilio.etp.server.config;

import cn.xilio.etp.common.StringUtils;
import cn.xilio.etp.common.TomlUtils;
import cn.xilio.etp.core.protocol.ProtocolType;
import com.moandjiezana.toml.Toml;

import java.util.*;

/**
 * 解析Toml配置文件内容
 *
 * @author liuxin
 */
public final class AppConfig {
    private final static AppConfig INSTANCE = new AppConfig();

    private AppConfig() {
    }

    public static AppConfig get() {
        return INSTANCE;
    }

    private String host;
    private int bindPort;
    private boolean tls;
    private KeystoreConfig keystoreConfig;
    private LogConfig logConfig;
    private Dashboard dashboard;
    private final List<ClientInfo> clients = new ArrayList<>();

    public AppConfig load(String path) {
        //顺序解析下面各种配置
        Toml root = TomlUtils.readToml(path);
        //解析Root配置
        parseRoot(root);
        //解析管理面板配置
        parseDashboard(root);
        //解析TLS加密配置
        parseTls(root);
        //解析客户端配置
        parseClient(root);
        //解析日志配置
        parseLogConfig(root);
        return this;
    }

    private void parseRoot(Toml root) {
        if (root.contains("bindPort")) {
            bindPort = root.getLong("bindPort").intValue();
        }
        if (root.contains("host")) {
            host = root.getString("host");
        }
        Boolean tlsValue = root.getBoolean("tls");
        tls = (tlsValue != null) ? tlsValue : false;
    }

    private void parseLogConfig(Toml root) {
        Toml log = root.getTable("log");
        if (log != null) {
            String level = log.getString("level");
            String pattern = log.getString("pattern");
            String path = log.getString("path");
            logConfig = new LogConfig(path, level, pattern);
        }
    }

    private void parseClient(Toml root) {
        List<Toml> readClients = root.getTables("clients");
        if (readClients == null) {
            return;
        }
        //创建两个集合用于临时缓存密钥和名字，用于重复校验
        Set<String> tokenTemp = new HashSet<>();
        Set<String> nameTemp = new HashSet<>();
        for (Toml client : readClients) {
            String name = client.getString("name");
            String secretKey = client.getString("secretKey");
            if (!StringUtils.hasText(name)) {
                throw new IllegalArgumentException("必须制定客户端的名称！");
            }
            if (tokenTemp.contains(secretKey)) {
                throw new IllegalArgumentException("客户端[密钥]冲突，不能存在重复的密钥！ " + secretKey);
            }
            if (nameTemp.contains(name)) {
                throw new IllegalArgumentException("客户端[名称]冲突，不能存在重复的名称！ " + name);
            }
            //解析客户端的所有端口映射信息
            List<ProxyMapping> proxies = parseProxes(client);
            //创建一个客户端
            ClientInfo clientInfo = new ClientInfo(secretKey);
            clientInfo.setName(name);
            clientInfo.setProxies(proxies);
            clients.add(clientInfo);
            tokenTemp.add(secretKey);
            nameTemp.add(name);
        }
    }

    private List<ProxyMapping> parseProxes(Toml client) {
        List<Toml> proxies = client.getTables("proxies");
        if (proxies == null) {
            return new ArrayList<>();
        }
        Set<Integer> portTemp = new HashSet<>();
        List<ProxyMapping> proxyMappings = new ArrayList<>();
        for (Toml proxy : proxies) {
            String type = proxy.getString("type");
            String proxyName = proxy.getString("name");
            Long localPort = proxy.getLong("localPort");
            Long remotePort = proxy.getLong("remotePort");
            Long status = proxy.getLong("status");
            if (!Objects.isNull(remotePort)) {
                if (portTemp.contains(remotePort.intValue())) {
                    throw new IllegalArgumentException("公网端口不能重复！" + remotePort.intValue());
                }
                portTemp.add(remotePort.intValue());
            }
            if (Objects.isNull(localPort)) {
                throw new IllegalArgumentException("必须指定内网端口");
            }
            ProxyMapping proxyMapping = new ProxyMapping(ProtocolType.getType(type), localPort.intValue(), remotePort == null ? null : remotePort.intValue());
            proxyMapping.setName(proxyName);
            proxyMapping.setStatus(status == null ? 1 : status.intValue());
            proxyMapping.setLocalPort(localPort.intValue());
            proxyMappings.add(proxyMapping);
        }
        return proxyMappings;
    }

    private void parseDashboard(Toml root) {
        Toml dash = root.getTable("dashboard");
        if (dash != null) {
            Boolean enable = dash.getBoolean("enable") != null && dash.getBoolean("enable");
            String addr = dash.getString("addr");
            Integer port = dash.getLong("port") == null ? null : dash.getLong("port").intValue();
            String username = dash.getString("username");
            String password = dash.getString("password");
            dashboard = new Dashboard(enable, username, password, addr, port);
        }
    }

    private void parseTls(Toml root) {
        Toml keystore = root.getTable("keystore");
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

    public String getHost() {
        return host;
    }

    public int getBindPort() {
        return bindPort;
    }

    public boolean isTls() {
        return tls;
    }

    public KeystoreConfig getKeystoreConfig() {
        return keystoreConfig;
    }

    public LogConfig getLogConfig() {
        return logConfig;
    }

    public Dashboard getDashboard() {
        return dashboard;
    }

    public List<ClientInfo> getClients() {
        return clients;
    }
}
