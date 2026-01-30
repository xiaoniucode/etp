package com.xiaoniucode.etp.server.web.service;

import com.xiaoniucode.etp.server.web.domain.Client;
import java.util.List;

public interface ClientService {
    Client getById(long id);
    Client getByName(String name);
    Client getBySecretKey(String secretKey);
    List<Client> list();
    boolean update(long id, String name);
    boolean deleteById(long id);
    int count();
}
