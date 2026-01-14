package com.xiaoniucode.etp.server.web.serivce;

import com.xiaoniucode.etp.core.msg.KickoutClient;
import com.xiaoniucode.etp.server.config.AuthInfo;
import com.xiaoniucode.etp.server.config.ClientInfo;
import com.xiaoniucode.etp.server.manager.ChannelManager;
import com.xiaoniucode.etp.server.manager.RuntimeStateManager;
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
    private final RuntimeStateManager state = RuntimeStateManager.get();
    public void kickoutClient(JSONObject req) {
        String secretKey = req.getString("secretKey");
        Channel control = ChannelManager.getControlChannelBySecretKey(secretKey);
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
            state.updateClientName(client.getString("secretKey"), name);
            DaoFactory.INSTANCE.getClientDao().update(id, name);
            return null;
        });

    }

    public void deleteClient(JSONObject req) {
        TX.execute(() -> {
            JSONObject client = DaoFactory.INSTANCE.getClientDao().getById(req.getInt("id"));
            int id = client.getInt("id");
            String secretKey = client.getString("secretKey");

            state.removeClient(secretKey);
            ChannelManager.closeControlChannelByClient(secretKey);
            //关闭该客户端所有运行状态的代理服务
            state.getClientRemotePorts(secretKey).forEach(remotePort -> {
                TcpProxyServer.get().stopRemotePort(remotePort, true);
            });
            DaoFactory.INSTANCE.getClientDao().deleteById(id);
            //删除客户端所有的代理映射
             ServiceFactory.INSTANCE.getProxyService().deleteProxiesByClient(id);
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
            AuthInfo authInfo = ChannelManager.getAuthInfo(secretKey);
            if (authInfo != null) {
                client.put("os", authInfo.getOs());
                client.put("arch", authInfo.getArch());
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
            ClientInfo clientInfo = new ClientInfo(clientId, name, secretKey);
            //注册客户端
            state.registerClient(clientInfo);
            return clientId;
        });
    }
}
