package cn.xilio.vine.server.store;

import cn.xilio.vine.core.command.model.ClientModel;
import cn.xilio.vine.core.command.model.ProxyModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 基于文件的代理规则存储
 */

public class FileProxyRuleStore implements ProxyRuleStore {

    @Override
    public void addClient(String name) {
        String uuid = UUID.randomUUID().toString();
        String secretKey = uuid.replaceAll("-", "");


    }

    @Override
    public List<ClientModel> getClients() {
        List<ClientInfo> clients = ProxyManager.getInstance().getClients();
        return clients.stream().map(client -> {
            ClientModel dto = new ClientModel();
            dto.setName(client.getName());
            dto.setStatus(client.getStatus());
            dto.setSecretKey(client.getSecretKey());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ProxyModel> getProxies() {
        List<ClientInfo> clients = ProxyManager.getInstance().getClients();
        ArrayList<ProxyModel> res = new ArrayList<>();
        for (ClientInfo client : clients) {
            for (ProxyMapping proxyMapping : client.getProxyMappings()) {
                ProxyModel proxyModel = new ProxyModel();
                proxyModel.setName(proxyMapping.getName());
                proxyModel.setSecretKey(client.getSecretKey());
                proxyModel.setType(proxyMapping.getType());
                proxyModel.setLocalIP(proxyMapping.getLocalIP());
                proxyModel.setLocalPort(proxyMapping.getLocalPort());
                proxyModel.setRemotePort(proxyMapping.getRemotePort());
                res.add(proxyModel);
            }
        }
        return res;
    }
}
