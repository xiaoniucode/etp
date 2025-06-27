package cn.xilio.vine.server.store;

import cn.xilio.vine.core.command.model.ClientModel;
import cn.xilio.vine.core.command.model.ProxyModel;

import java.util.List;

/**
 * 代理规则存储接口
 */
public interface ProxyRuleStore {
    /**
     * 创建一个客户端
     */
    public void addClient(ClientModel clientModel);

    /**
     * 获取所有的客户端列表
     *
     * @return 客户端列表
     */
    public List<ClientModel> getClients();

    /**
     * 根据密钥删除客户端
     *
     * @param secretKey 密钥，唯一标识
     * @return 是否删除成功
     */
    public boolean deleteClient(String secretKey);

    /**
     * 更新客户端信息
     *
     * @param proxyModel 更新信息
     */
    public void updateClient(ProxyModel proxyModel);

    /**
     * 添加一个代理规则
     *
     * @param proxyModel 代理信息
     */
    public void addProxy(ProxyModel proxyModel);

    /**
     * 获取所有的代理配置信息，包括所有客户端的配置
     *
     * @return 代理配置列表
     */
    public List<ProxyModel> getProxies();

    /**
     * 根据远程端口删除代理
     *
     * @param remotePort 公网端口
     * @return 是否删除成功
     */
    public boolean deleteProxy(int remotePort);

    /**
     * 更新代理信息
     *
     * @param proxyModel 需要更新的信息
     */
    public void updateProxy(ProxyModel proxyModel);

}
