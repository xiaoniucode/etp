package com.xiaoniucode.etp.server.web;

import com.xiaoniucode.etp.common.StringUtils;
import com.xiaoniucode.etp.core.protocol.ProtocolType;
import com.xiaoniucode.etp.server.config.AuthInfo;
import com.xiaoniucode.etp.server.config.PortRange;
import com.xiaoniucode.etp.server.manager.ChannelManager;
import com.xiaoniucode.etp.server.manager.PortAllocator;
import com.xiaoniucode.etp.server.proxy.TcpProxyServer;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.config.ClientInfo;
import com.xiaoniucode.etp.server.config.ProxyMapping;
import com.xiaoniucode.etp.server.manager.RuntimeState;
import com.xiaoniucode.etp.server.web.digest.DigestUtil;
import com.xiaoniucode.etp.server.web.manager.CaptchaHolder;
import com.xiaoniucode.etp.server.web.manager.TokenAuthService;
import com.xiaoniucode.etp.server.web.server.BizException;
import com.xiaoniucode.etp.server.web.transaction.SQLiteTransactionTemplate;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 配置服务：客户端、端口映射、用户信息的管理、登录认证
 *
 * @author liuxin
 */
public final class ConfigService {
    private static Logger logger = LoggerFactory.getLogger(ConfigService.class);
    private final static ConfigStore configStore = new ConfigStore();
    private final static AppConfig config = AppConfig.get();
    private final static RuntimeState state = RuntimeState.get();
    private static final SQLiteTransactionTemplate TX = new SQLiteTransactionTemplate();

    public static void addClient(JSONObject client) {
        TX.execute(() -> {
            String name = client.getString("name");
            JSONObject existClient = configStore.getClientByName(name);
            if (existClient != null) {
                throw new BizException("名称不能重复");
            }
            String secretKey = UUID.randomUUID().toString().replaceAll("-", "");
            client.put("secretKey", secretKey);
            //添加到数据库
            int clientId = configStore.addClient(client);
            ClientInfo clientInfo = new ClientInfo(clientId, name, secretKey);
            //注册客户端
            state.registerClient(clientInfo);
            return null;
        });
    }

    public static JSONObject monitorInfo() {
        JSONObject res = new JSONObject();
        JSONObject stats = new JSONObject();
        JSONArray clients = clients();
        JSONArray proxies = proxies();
        stats.put("clientTotal", clients.length());
        stats.put("onlineClient", ChannelManager.onlineClientCount());
        stats.put("mappingTotal", proxies.length());
        stats.put("startMapping", TcpProxyServer.get().runningPortCount());
        JSONObject sysConfig = getSystemConfig();
        res.put("stats", stats);
        res.put("sysConfig", sysConfig);
        return res;
    }

    public static JSONArray clients() {
        JSONArray clients = configStore.listClients();
        for (int i = 0; i < clients.length(); i++) {
            JSONObject client = clients.getJSONObject(i);
            String secretKey = client.getString("secretKey");
            client.put("status", ChannelManager.clientIsOnline(client.getString("secretKey")) ? 1 : 0);
            //获取在线客户端信息
            AuthInfo authInfo = ChannelManager.getAuthInfo(secretKey);
            if (authInfo != null) {
                client.put("os", authInfo.getOs());
                client.put("arch", authInfo.getArch());
            }
        }
        return clients;
    }

    public static void deleteAllAutoRegisterProxy() {
        configStore.deleteAllAutoRegisterProxy();
    }

    public static JSONArray proxies() {
        return configStore.listAllProxies();
    }

    public static JSONObject addProxy(JSONObject req) {
        return TX.execute(() -> {
            int remotePort = req.getInt("remotePort");
            if (remotePort != -1 && !PortAllocator.get().isPortAvailable(remotePort)) {
                throw new BizException("映射注册失败，公网端口: " + remotePort + "无效！");
            }
            String secretKey = req.getString("secretKey");
            if (state.isPortOccupied(remotePort)) {
                throw new BizException("公网端口:" + remotePort + "已被占用");
            }
            //-1表示用户没有自定义端口
            if (remotePort == -1) {
                int allocatePort = PortAllocator.get().allocateAvailablePort();
                req.put("remotePort", allocatePort);
            }
            if (!StringUtils.hasText(req.getString("name"))) {
                req.put("name", req.getInt("remotePort"));
            }
            JSONObject res = new JSONObject();
            int proxyId = configStore.addProxy(req);
            int remotePortInt = req.getInt("remotePort");
            //注册端口映射
            state.registerProxy(secretKey, createProxyMapping(req));
            //如果客户度已经启动认证
            ChannelManager.addPortToControlChannelIfOnline(secretKey, remotePortInt);
            //如果状态是1，需要启动代理服务
            if (req.getInt("status") == 1) {
                TcpProxyServer.get().startRemotePort(remotePortInt);
            } else {
                //将公网端口添加到已分配缓存
                PortAllocator.get().addRemotePort(remotePortInt);
            }
            res.put("proxyId", proxyId);
            res.put("remotePort", remotePortInt);
            return res;
        });
    }

    public static void updateProxy(JSONObject req) {
        TX.execute(() -> {
            String newRemotePortString = req.getString("remotePort");
            int newRemotePortInt;

            JSONObject oldProxy = configStore.getProxyById(req.getInt("id"));
            int oldRemotePort = oldProxy.getInt("remotePort");
            //如果没有设置公网端口，自动分配一个
            boolean remotePortChanged = false;
            if (!StringUtils.hasText(newRemotePortString)) {
                int allocatePort = PortAllocator.get().allocateAvailablePort();
                req.put("remotePort", allocatePort);
                newRemotePortInt = allocatePort;
                remotePortChanged = true;
            } else {
                newRemotePortInt = req.getInt("remotePort");
                //如果有公网端口，如果发生改变，需要判断端口是否可用
                if (newRemotePortInt != oldRemotePort) {
                    remotePortChanged = true;
                    if (state.isPortOccupied(newRemotePortInt)) {
                        throw new BizException("公网端口已被占用");
                    }
                    if (!PortAllocator.get().isPortAvailable(newRemotePortInt)) {
                        throw new BizException("公网端口无效");
                    }
                }
            }
            //检查名称
            String oldName = oldProxy.getString("name");
            String newName = req.getString("name");
            String secretKey = req.getString("secretKey");
            if (!oldName.equals(newName) && (configStore.getProxy(req.getInt("clientId"), newName) != null)) {
                throw new BizException("该客户端存在同名映射");
            }
            configStore.updateProxy(req);
            //删除已经注册的映射
            state.removeProxy(secretKey, oldRemotePort);
            //重新注册更新后的映射
            state.registerProxy(secretKey, createProxyMapping(req));
            //如果客户度已经启动认证
            ChannelManager.addPortToControlChannelIfOnline(secretKey, newRemotePortInt);
            //公网端口发生更新，需要停掉之前的服务连接
            if (remotePortChanged) {
                TcpProxyServer.get().stopRemotePort(oldRemotePort, true);
            }
            //如果状态是1，需要启动代理服务
            if (req.getInt("status") == 1) {
                TcpProxyServer.get().startRemotePort(newRemotePortInt);
            } else {
                //停止对应端口的代理映射服务
                TcpProxyServer.get().stopRemotePort(newRemotePortInt, false);
            }
            return null;
        });
    }

    private static ProxyMapping createProxyMapping(JSONObject req) {
        ProxyMapping proxyMapping = new ProxyMapping(ProtocolType.getType(req.getString("type")),
            req.getInt("localPort"),
            req.getInt("remotePort"));
        proxyMapping.setProxyId(req.getInt("clientId"));
        proxyMapping.setName(req.getString("name"));
        proxyMapping.setStatus(req.getInt("status"));
        return proxyMapping;
    }

    /**
     * 切换端口映射状态
     */
    public static void switchProxyStatus(JSONObject req) {
        TX.execute(() -> {
            JSONObject proxy = configStore.getProxyById(req.getInt("id"));
            int status = proxy.getInt("status");
            String secretKey = req.getString("secretKey");
            int remotePort = proxy.getInt("remotePort");
            int updateStatus = status == 1 ? 0 : 1;
            state.updateProxyStatus(secretKey, remotePort, updateStatus);
            if (updateStatus == 1) {
                TcpProxyServer.get().startRemotePort(remotePort);
            } else {
                TcpProxyServer.get().stopRemotePort(remotePort, false);
            }
            //持久化数据库
            proxy.put("status", updateStatus);
            configStore.updateProxy(proxy);
            return null;
        });
    }

    public static void deleteProxy(JSONObject req) {
        TX.execute(() -> {
            int id = req.getInt("id");
            JSONObject proxy = configStore.getProxyById(id);
            String secretKey = req.getString("secretKey");
            int remotePort = proxy.getInt("remotePort");
            //删除注册的端口映射
            state.removeProxy(secretKey, remotePort);
            //删除公网端口与已认证客户端的绑定
            ChannelManager.removeRemotePortToControlChannel(remotePort);
            //停掉连接的服务并释放端口
            TcpProxyServer.get().stopRemotePort(remotePort, true);
            configStore.deleteProxy(id);
            return null;
        });
    }

    public static void updateClient(JSONObject req) {
        TX.execute(() -> {
            int id = req.getInt("id");
            String name = req.getString("name");
            JSONObject client = configStore.getClientById(id);
            if (!client.getString("name").equals(name)) {
                JSONObject oldClient = configStore.getClientByName(name);
                if (oldClient != null) {
                    throw new BizException("客户端名不能重复");
                }
            }
            state.updateClientName(client.getString("secretKey"), name);
            configStore.updateClient(id, name);
            return null;
        });

    }

    public static void deleteClient(JSONObject req) {
        TX.execute(() -> {
            JSONObject client = configStore.getClientById(req.getInt("id"));
            int id = client.getInt("id");
            String secretKey = client.getString("secretKey");

            state.removeClient(secretKey);
            ChannelManager.closeControlChannelByClient(secretKey);
            //关闭该客户端所有运行状态的代理服务
            state.getClientRemotePorts(secretKey).forEach(remotePort -> {
                TcpProxyServer.get().stopRemotePort(remotePort, true);
            });
            configStore.deleteClient(id);
            //删除客户端所有的代理映射
            configStore.deleteProxiesByClient(id);
            return null;
        });

    }

    /**
     * 将在线的客户端踢下线
     */
    public static void kickoutClient(JSONObject req) {
        String secretKey = req.getString("secretKey");
        //关闭客户端控制隧道，同时关闭所有连接
        ChannelManager.closeControlChannelByClient(secretKey);
    }

    public static JSONObject getClient(JSONObject req) {
        return configStore.getClientById(req.getInt("id"));
    }

    public static JSONObject getProxy(JSONObject req) {
        return configStore.getProxyById(req.getInt("id"));
    }

    public static JSONObject login(JSONObject req) {
        String captchaId = req.optString("captchaId");
        String code = req.optString("code");
        if (!CaptchaHolder.verifyAndRemove(captchaId, code)) {
            throw new BizException("验证码过期");
        }
        String username = req.optString("username");
        String password = req.optString("password");
        JSONObject user = configStore.getUserByUsername(username);
        if (user == null) {
            throw new BizException(401, "用户不存在！");
        }
        //检查密码
        if (!DigestUtil.encode(password, username).equals(user.optString("password"))) {
            throw new BizException(401, "密码错误");
        }
        //创建登录令牌
        return TokenAuthService.createToken(user.getInt("id"), username);
    }

    public static JSONObject getUserByUsername(String username) {
        return configStore.getUserByUsername(username);
    }

    public static void registerUser(JSONObject user) {
        configStore.addUser(user);
    }

    public static void deleteAll() {
        configStore.deleteAllUser();
    }

    public static void updateUserPassword(Integer userId, JSONObject req) {
        TX.execute(() -> {
            int id = req.getInt("userId");
            String oldPassword = req.getString("oldPassword");
            String newPassword = req.getString("password");
            if (userId != id) {
                throw new BizException(401, "未登录");
            }
            JSONObject user = configStore.getUserById(userId);
            String username = user.getString("username");
            if (!user.getString("password").equals(DigestUtil.encode(oldPassword, username))) {
                throw new BizException("原密码不正确");
            }
            configStore.updateUserPassword(id, DigestUtil.encode(newPassword, username));
            return null;
        });

    }

    public static JSONObject getSystemSetting(String key) {
        return configStore.findSettingByKey(key);
    }

    public static void addSystemSetting(JSONObject save) {
        configStore.addSetting(save);
    }

    private static JSONObject getSystemConfig() {
        JSONObject res = new JSONObject();
        PortRange range = config.getPortRange();
        res.put("tls_enabled", config.isTls() ? "已开启" : "未开启");
        res.put("host", config.getHost());
        res.put("bind_port", config.getBindPort());

        res.put("port_range_start", range.getStart());
        res.put("port_range_end", range.getEnd());

        return res;
    }
}
