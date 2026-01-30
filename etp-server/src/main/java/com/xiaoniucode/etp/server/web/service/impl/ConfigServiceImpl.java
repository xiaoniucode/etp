package com.xiaoniucode.etp.server.web.service.impl;

import com.xiaoniucode.etp.server.web.service.ConfigService;
import com.xiaoniucode.etp.server.web.domain.Config;
import com.xiaoniucode.etp.server.web.repository.ConfigsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConfigServiceImpl implements ConfigService {

    @Autowired
    private ConfigsRepository configsRepository;

    @Override
    public int update(Config save) {
        configsRepository.save(save);
        return 1;
    }

    @Override
    public int insert(Config save) {
        configsRepository.save(save);
        return 1;
    }

    @Override
    public Config getByKey(String key) {
        return configsRepository.findByKey(key);
    }
}
