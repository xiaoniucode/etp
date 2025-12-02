package cn.xilio.etp.server.web;

import cn.xilio.etp.common.StringUtils;
import cn.xilio.etp.core.protocol.ProtocolType;
import cn.xilio.etp.server.manager.ChannelManager;
import cn.xilio.etp.server.manager.PortAllocator;
import cn.xilio.etp.server.TcpProxyServer;
import cn.xilio.etp.server.config.AppConfig;
import cn.xilio.etp.server.config.ClientInfo;
import cn.xilio.etp.server.config.ProxyMapping;
import cn.xilio.etp.server.manager.RuntimeState;
import cn.xilio.etp.server.web.framework.BizException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 配置服务
 *
 * @author liuxin
 */
public final class ConfigService {
    private static Logger logger = LoggerFactory.getLogger(ConfigService.class);
    private final static ConfigStore configStore = new ConfigStore();
    private final static AppConfig config = AppConfig.get();
    private final static RuntimeState state = RuntimeState.get();

    public static void addClient(JSONObject client) {
        JSONObject existClient = configStore.getClientByName(client.getString("name"));
        if (existClient != null) {
            throw new BizException("名称不能重复");
        }
        String secretKey = UUID.randomUUID().toString().replaceAll("-", "");
        client.put("secretKey", secretKey);
        //添加到数据库
        configStore.addClient(client);
        ClientInfo clientInfo = new ClientInfo(client.getString("secretKey"));
        clientInfo.setName(client.getString("name"));
        //注册客户端
        state.registerClient(clientInfo);
    }

    /**
     * 数据统计
     */
    public static JSONObject countStats() {
        JSONObject jsonObject = new JSONObject();
        JSONArray clients = clients();
        JSONArray proxies = proxies();
        jsonObject.put("clientTotal", clients.length());
        jsonObject.put("onlineClient", ChannelManager.onlineClientCount());
        jsonObject.put("mappingTotal", proxies.length());
        jsonObject.put("startMapping", TcpProxyServer.get().runningPortCount());
        return jsonObject;
    }

    public static JSONArray clients() {
        JSONArray clients = configStore.listClients();
        for (int i = 0; i < clients.length(); i++) {
            JSONObject client = clients.getJSONObject(i);
            client.put("status", ChannelManager.clientIsOnline(client.getString("secretKey")) ? 1 : 0);
        }
        return clients;
    }

    public static JSONArray proxies() {
        return configStore.listAllProxies();
    }


    public static void addProxy(JSONObject req) {
        String remotePortString = req.getString("remotePort");
        String secretKey = req.getString("secretKey");
        if (StringUtils.hasText(remotePortString) && state.isPortOccupied(req.getInt("remotePort"))) {
            throw new BizException("公网端口已被占用");
        }

        if (StringUtils.hasText(remotePortString) && !PortAllocator.get().isPortAvailable(req.getInt("remotePort"))) {
            throw new BizException("公网端口无效");
        }
        if (configStore.getProxyByName(req.getString("name")) != null) {
            throw new BizException("已存在相同名称的映射");
        }
        if (!StringUtils.hasText(remotePortString)) {
            int allocatePort = PortAllocator.get().allocateAvailablePort();
            req.put("remotePort", allocatePort);
        }
        configStore.addProxy(req);
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
    }

    public static void updateProxy(JSONObject req) {
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
        if (!oldName.equals(newName) && (configStore.getProxyByName(newName) != null)) {
            throw new BizException("映射名称已经存在");
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
    }

    public static void deleteProxy(JSONObject req) {
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
    }

    public static void updateClient(JSONObject req) {
        int id = req.getInt("id");
        String name = req.getString("name");
        JSONObject client = configStore.getClientById(id);
        state.updateClientName(client.getString("secretKey"), name);
        configStore.updateClient(id, name);
    }

    public static void deleteClient(JSONObject req) {
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
            throw new BizException("验证码错误或过期");
        }
        String username = req.optString("username");
        String password = req.optString("password");
        JSONObject user = configStore.getUserByUsername(username);
        if (user == null) {
            throw new BizException(401, "用户不存在！");
        }
        //检查密码 todo 暂时不加密
        if (!password.equals(user.optString("password"))) {
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
        int id = req.getInt("userId");
        String oldPassword = req.getString("oldPassword");
        String newPassword = req.getString("password");
        if (userId != id) {
            throw new BizException(401, "未登录");
        }
        JSONObject user = configStore.getUserById(userId);
        if (!user.getString("password").equals(oldPassword)) {
            throw new BizException("原密码不正确");
        }
        configStore.updateUserPassword(id, newPassword);
    }
}
