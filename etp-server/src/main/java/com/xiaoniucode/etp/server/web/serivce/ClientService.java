package com.xiaoniucode.etp.server.web.serivce;

import com.xiaoniucode.etp.core.msg.KickoutClient;
import com.xiaoniucode.etp.core.AuthClientInfo;
import com.xiaoniucode.etp.server.config.domain.ClientInfo;
import com.xiaoniucode.etp.server.manager.ClientManager;
import com.xiaoniucode.etp.server.manager.ProxyManager;
import com.xiaoniucode.etp.server.manager.ChannelManager;
import com.xiaoniucode.etp.server.proxy.TcpProxyServer;
import com.xiaoniucode.etp.server.web.core.orm.transaction.JdbcTransactionTemplate;
import com.xiaoniucode.etp.server.web.core.server.BizException;
import com.xiaoniucode.etp.server.web.dao.DaoFactory;
import io.netty.channel.Channel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.UUID;

public class ClientService {
    private final JdbcTransactionTemplate TX = new JdbcTransactionTemplate();

    public void kickoutClient(JSONObject req) {
        String secretKey = req.getString("secretKey");
        Channel control = ChannelManager.getControl(secretKey);
        if (control != null) {
            control.writeAndFlush(new KickoutClient());
        }
    }

    public JSONObject getClient(JSONObject req) {
        return DaoFactory.INSTANCE.getClientDao().getById(req.getInt("id"));
    }

    public void updateClient(JSONObject req) {
        TX.execute(() -> {
            int id = req.getInt("id");
            String name = req.getString("name");
            JSONObject client = DaoFactory.INSTANCE.getClientDao().getById(id);
            if (!client.getString("name").equals(name)) {
                JSONObject oldClient = DaoFactory.INSTANCE.getClientDao().getByName(name);
                if (oldClient != null) {
                    throw new BizException("客户端名不能重复");
                }
            }
            ClientManager.updateClientName(client.getString("secretKey"), name);
            DaoFactory.INSTANCE.getClientDao().update(id, name);
            return null;
        });

    }

    //todo 客户端下线有bug
    public void deleteClient(JSONObject req) {
        TX.execute(() -> {
            JSONObject client = DaoFactory.INSTANCE.getClientDao().getById(req.getInt("id"));
            int id = client.getInt("id");
            String secretKey = client.getString("secretKey");

            ClientManager.removeClient(secretKey);
            ChannelManager.closeControl(secretKey);
            //关闭该客户端所有运行状态的代理服务
            ProxyManager.getClientRemotePorts(secretKey).forEach(remotePort -> {
                TcpProxyServer.get().stopRemotePort(remotePort, true);
            });
            DaoFactory.INSTANCE.getClientDao().deleteById(id);
            //删除客户端所有的代理映射
            ServiceFactory.INSTANCE.getProxyService().deleteProxiesByClient(id);
            //发消息通知客户端断开连接
            Channel control = ChannelManager.getControl(secretKey);
            if (control != null) {
                control.writeAndFlush(new KickoutClient());
            }
            return null;
        });

    }

    public JSONArray clients() {
        JSONArray clients = DaoFactory.INSTANCE.getClientDao().list();
        for (int i = 0; i < clients.length(); i++) {
            JSONObject client = clients.getJSONObject(i);
            String secretKey = client.getString("secretKey");
            client.put("status", ChannelManager.clientIsOnline(client.getString("secretKey")) ? 1 : 0);
            //获取在线客户端信息
            AuthClientInfo authClientInfo = ChannelManager.getAuthClientInfo(secretKey);
            if (authClientInfo != null) {
                client.put("os", authClientInfo.getOs());
                client.put("arch", authClientInfo.getArch());
            }
        }
        return clients;
    }

    public int addClient(JSONObject client) {
        return TX.execute(() -> {
            String name = client.getString("name");
            JSONObject existClient = DaoFactory.INSTANCE.getClientDao().getByName(name);
            if (existClient != null) {
                throw new BizException("名称不能重复");
            }
            String secretKey = UUID.randomUUID().toString().replaceAll("-", "");
            client.put("secretKey", secretKey);
            //添加到数据库
            int clientId = DaoFactory.INSTANCE.getClientDao().insert(client);
            ClientInfo clientInfo = new ClientInfo(name, secretKey, clientId);
            //注册客户端
            ClientManager.addClient(clientInfo);
            return clientId;
        });
    }
}
