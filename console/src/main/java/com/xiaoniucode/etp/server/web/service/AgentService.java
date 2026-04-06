package com.xiaoniucode.etp.server.web.service;

import com.xiaoniucode.etp.server.web.controller.agent.request.BatchDeleteClientRequest;
import com.xiaoniucode.etp.server.web.controller.agent.request.ClientSaveRequest;
import com.xiaoniucode.etp.server.web.controller.agent.response.AgentDTO;

import java.util.List;

public interface AgentService {
    /**
     * 查询所有客户端
     */
    List<AgentDTO> findAll(String keyword, int page, int size);

    /**
     * 根据 ID 查询客户端
     */
    AgentDTO findById(String clientId);

    /**
     * 删除单个客户端
     */
    void delete(String clientId);

    /**
     * 批量删除客户端
     */
    void deleteBatch( BatchDeleteClientRequest request);

    /**
     * 剔除在线客户端
     */
    void kickout(String clientId);

    /**
     * 保存客户端
     *
     */
    void saveClient(ClientSaveRequest request);
}



