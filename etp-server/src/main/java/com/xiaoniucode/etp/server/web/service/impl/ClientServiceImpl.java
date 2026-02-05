package com.xiaoniucode.etp.server.web.service.impl;

import com.xiaoniucode.etp.server.web.common.BizException;
import com.xiaoniucode.etp.server.web.domain.Client;
import com.xiaoniucode.etp.server.web.repository.ClientRepository;
import com.xiaoniucode.etp.server.web.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientServiceImpl implements ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Override
    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    @Override
    public Client findById(Integer id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new BizException("客户端不存在"));
    }

    @Override
    public void delete(Integer id) {
        clientRepository.deleteById(id);
    }

    @Override
    public void deleteBatch(List<Integer> ids) {
        clientRepository.deleteAllById(ids);
    }

    @Override
    public Client kickout(Integer id) {
        Client client = findById(id);
        client.setStatus(0); // 0 表示离线
        return clientRepository.save(client);
    }
}



