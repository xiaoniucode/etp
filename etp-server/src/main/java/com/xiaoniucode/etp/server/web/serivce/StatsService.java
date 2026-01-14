package com.xiaoniucode.etp.server.web.serivce;

import com.xiaoniucode.etp.server.manager.ChannelManager;
import com.xiaoniucode.etp.server.proxy.TcpProxyServer;
import org.json.JSONArray;
import org.json.JSONObject;

public class StatsService {
    public JSONObject monitorInfo() {
        JSONObject res = new JSONObject();
        JSONObject stats = new JSONObject();
        JSONArray clients = ServiceFactory.INSTANCE.getClientService().clients();
        JSONArray proxies = ServiceFactory.INSTANCE.getProxyService().proxies(null);
        stats.put("clientTotal", clients.length());
        stats.put("onlineClient", ChannelManager.onlineClientCount());
        stats.put("mappingTotal", proxies.length());
        stats.put("startMapping", TcpProxyServer.get().runningPortCount());
        JSONObject sysConfig = ServiceFactory.INSTANCE.getSettingService().getAppConfig();
        res.put("stats", stats);
        res.put("sysConfig", sysConfig);
        return res;
    }
}
