package com.xiaoniucode.etp.server.web.service;

import com.xiaoniucode.etp.server.web.entity.Config;

public interface ConfigService {
    int update(Config save);
    int insert(Config save);
    Config getByKey(String key);
}
