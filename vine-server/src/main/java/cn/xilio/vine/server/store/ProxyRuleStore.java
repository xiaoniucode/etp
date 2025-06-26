package cn.xilio.vine.server.store;

import cn.xilio.vine.core.command.model.ClientModel;
import cn.xilio.vine.core.command.model.ProxyModel;

import java.util.List;

/**
 * 代理规则存储接口
 */
public interface ProxyRuleStore {

    public void addClient(String name);
    public List<ClientModel> getClients();
//    public void deleteClient();
//    public void updateClient();
//
//    public void addProxy();
    public List<ProxyModel> getProxies();
//    public void deleteProxy();
//    public void updateProxy();

}
