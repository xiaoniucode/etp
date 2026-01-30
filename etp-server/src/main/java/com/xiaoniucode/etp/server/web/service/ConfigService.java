package com.xiaoniucode.etp.server.web.service;

import com.xiaoniucode.etp.server.web.domain.Config;

public interface ConfigService {
    int update(Config save);
    int insert(Config save);
    Config getByKey(String key);
}
