package com.xiaoniucode.etp.server.web.serivce;

import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.config.PortRange;
import com.xiaoniucode.etp.server.web.dao.DaoFactory;
import org.json.JSONObject;

public class SettingService {
    private final AppConfig config = AppConfig.get();

    public JSONObject getSetting(String key) {
        return DaoFactory.INSTANCE.getSettingDao().getByKey(key);
    }

    public void addSetting(JSONObject save) {
        DaoFactory.INSTANCE.getSettingDao().insert(save);
    }

    public JSONObject getAppConfig() {
        JSONObject res = new JSONObject();
        PortRange range = config.getPortRange();
        res.put("tls_enabled", config.isTls() ? "已开启" : "未开启");
        res.put("host", config.getHost());
        res.put("bind_port", config.getBindPort());

        res.put("port_range_start", range.getStart());
        res.put("port_range_end", range.getEnd());

        return res;
    }
}
