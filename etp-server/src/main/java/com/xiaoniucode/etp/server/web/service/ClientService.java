package com.xiaoniucode.etp.server.web.service;

import com.xiaoniucode.etp.server.web.controller.client.response.ClientDTO;

import java.util.List;

public interface ClientService {
    /**
     * 查询所有客户端
     */
    List<ClientDTO> findAll();

    /**
     * 根据 ID 查询客户端
     */
    ClientDTO findById(String clientId);

    /**
     * 删除单个客户端
     */
    void delete(String clientId);

    /**
     * 批量删除客户端
     */
    void deleteBatch(List<String> ids);

    /**
     * 剔除在线客户端
     */
    void kickout(String clientId);
}



