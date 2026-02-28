package com.xiaoniucode.etp.server.web.service.impl;

import com.xiaoniucode.etp.server.web.service.ConfigService;
import com.xiaoniucode.etp.server.web.entity.Config;
import com.xiaoniucode.etp.server.web.repository.ConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConfigServiceImpl implements ConfigService {

    @Autowired
    private ConfigRepository configRepository;

    @Override
    public int update(Config save) {
        configRepository.save(save);
        return 1;
    }

    @Override
    public int insert(Config save) {
        configRepository.save(save);
        return 1;
    }

    @Override
    public Config getByKey(String key) {
        return configRepository.findByConfigKey(key);
    }
}
