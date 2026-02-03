package com.xiaoniucode.etp.server.web.service.impl;

import com.xiaoniucode.etp.server.web.service.ClientService;
import com.xiaoniucode.etp.server.web.domain.Client;
import com.xiaoniucode.etp.server.web.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ClientServiceImpl implements ClientService {

    @Autowired
    private ClientRepository clientsRepository;

    @Override
    public Client getById(long id) {
        return clientsRepository.findById((int) id).orElse(null);
    }

    @Override
    public Client getByName(String name) {
        return clientsRepository.findByName(name);
    }

    @Override
    public Client getBySecretKey(String secretKey) {
        return null;
    }

    @Override
    public List<Client> list() {
        return clientsRepository.findAll();
    }

    @Override
    public boolean update(long id, String name) {
        Client client = clientsRepository.findById((int) id).orElse(null);
        if (client != null) {
            client.setName(name);
            clientsRepository.save(client);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteById(long id) {
        clientsRepository.deleteById((int) id);
        return true;
    }

    @Override
    public int count() {
        return (int) clientsRepository.count();
    }
}
