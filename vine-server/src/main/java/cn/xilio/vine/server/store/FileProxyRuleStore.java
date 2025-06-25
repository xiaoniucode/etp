package cn.xilio.vine.server.store;

import cn.xilio.vine.core.command.model.ClientModel;
import cn.xilio.vine.server.store.dto.ClientInfoDTO;
import cn.xilio.vine.server.store.dto.ProxyInfoDTO;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基于文件的代理规则存储
 */

public class FileProxyRuleStore implements ProxyRuleStore {

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
    public List<ProxyInfoDTO> getProxies() {
        return Collections.emptyList();
    }
}
