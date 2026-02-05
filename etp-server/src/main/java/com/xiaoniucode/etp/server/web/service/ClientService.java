package com.xiaoniucode.etp.server.web.service;

import com.xiaoniucode.etp.server.web.domain.Client;

import java.util.List;

public interface ClientService {
    /**
     * 查询所有客户端
     */
    List<Client> findAll();

    /**
     * 根据 ID 查询客户端
     */
    Client findById(Integer id);

    /**
     * 删除单个客户端
     */
    void delete(Integer id);

    /**
     * 批量删除客户端
     */
    void deleteBatch(List<Integer> ids);

    /**
     * 剔除客户端（切换状态为离线）
     */
    Client kickout(Integer id);
}



