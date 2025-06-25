package cn.xilio.vine.server.store;

import cn.xilio.vine.server.store.dto.ClientInfoDTO;
import cn.xilio.vine.server.store.dto.ProxyInfoDTO;

import java.util.List;

/**
 * 代理规则存储接口
 */
public interface ProxyRuleStore {

   // public void addClient();
    public List<ClientInfoDTO> getClients();
//    public void deleteClient();
//    public void updateClient();
//
//    public void addProxy();
    public List<ProxyInfoDTO> getProxies();
//    public void deleteProxy();
//    public void updateProxy();

}
