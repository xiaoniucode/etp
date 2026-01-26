package com.xiaoniucode.etp.server.web.serivce;

import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.config.ConfigHelper;
import com.xiaoniucode.etp.server.config.domain.PortRange;
import com.xiaoniucode.etp.server.web.dao.DaoFactory;
import org.json.JSONObject;

public class SettingService {
    public JSONObject getSetting(String key) {
        return DaoFactory.INSTANCE.getSettingDao().getByKey(key);
    }

    public void addSetting(JSONObject save) {
        DaoFactory.INSTANCE.getSettingDao().insert(save);
    }

    public JSONObject getAppConfig() {
        AppConfig config = ConfigHelper.get();
        JSONObject res = new JSONObject();
        PortRange range = config.getPortRange();
        res.put("tls_enabled", config.isTls() ? "已开启" : "未开启");
        res.put("baseDomains", config.getBaseDomains());
        res.put("host", config.getHost());
        res.put("bind_port", config.getBindPort());
        res.put("httpProxyPort",config.getHttpProxyPort());
        res.put("httpsProxyPort",config.getHttpsProxyPort());

        res.put("port_range_start", range.getStart());
        res.put("port_range_end", range.getEnd());

        return res;
    }
}
