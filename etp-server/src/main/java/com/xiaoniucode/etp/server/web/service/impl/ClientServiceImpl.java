package com.xiaoniucode.etp.server.web.service.impl;

import com.xiaoniucode.etp.server.manager.session.AgentSessionManager;
import com.xiaoniucode.etp.server.web.common.BizException;
import com.xiaoniucode.etp.server.web.controller.client.convert.ClientConvert;
import com.xiaoniucode.etp.server.web.controller.client.request.BatchDeleteClientRequest;
import com.xiaoniucode.etp.server.web.controller.client.request.ClientSaveRequest;
import com.xiaoniucode.etp.server.web.controller.client.response.ClientDTO;
import com.xiaoniucode.etp.server.web.domain.Client;
import com.xiaoniucode.etp.server.web.repository.ClientRepository;
import com.xiaoniucode.etp.server.web.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClientServiceImpl implements ClientService {

    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private AgentSessionManager agentSessionManager;

    @Override
    public List<ClientDTO> findAll() {
        List<Client> clients = clientRepository.findAll();
        List<ClientDTO> clientDTOs = ClientConvert.INSTANCE.toDTOList(clients);
        for (ClientDTO clientDTO : clientDTOs) {
            boolean isOnline = agentSessionManager.isOnline(clientDTO.getId());
            clientDTO.setIsOnline(isOnline);
        }
        return clientDTOs;
    }

    @Override
    public ClientDTO findById(String id) {
        Client client = clientRepository.findById(id).orElseThrow(() -> new BizException("客户端不存在"));
        ClientDTO clientDTO = ClientConvert.INSTANCE.toDTO(client);
        boolean isOnline = agentSessionManager.isOnline(id);
        clientDTO.setIsOnline(isOnline);
        return clientDTO;
    }

    @Override
    public void delete(String id) {
        clientRepository.deleteById(id);
    }

    @Override
    public void deleteBatch(BatchDeleteClientRequest request) {
        clientRepository.deleteAllById(request.getIds());
    }

    @Override
    public void kickout(String clientId) {
        agentSessionManager.kickoutAgent(clientId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveClient(ClientSaveRequest request) {
        Client client = ClientConvert.INSTANCE.toEntity(request);
        clientRepository.saveAndFlush(client);
    }
}



