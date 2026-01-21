package com.xiaoniucode.etp.server.web.serivce;

import com.xiaoniucode.etp.server.manager.ChannelManager;
import com.xiaoniucode.etp.server.manager.ClientManager;
import com.xiaoniucode.etp.server.proxy.TcpProxyServer;
import com.xiaoniucode.etp.server.web.common.ServerHelper;
import org.json.JSONArray;
import org.json.JSONObject;

public class StatsService {
    public JSONObject monitorInfo() {
        JSONObject res = new JSONObject();
        JSONObject stats = new JSONObject();
        JSONArray proxies = ServiceFactory.INSTANCE.getProxyService().proxies(null);
        int tcpCount = 0;
        int httpCount = 0;
        int httpsCount = 0;
        for (Object p : proxies) {
            JSONObject proxy = (JSONObject) p;
            if (proxy.getString("type").equalsIgnoreCase("TCP")) {
                tcpCount++;
            } else if (proxy.getString("type").equalsIgnoreCase("HTTP")) {
                httpCount++;
            } else if (proxy.getString("type").equalsIgnoreCase("HTTPS")) {
                httpsCount++;
            }
        }
        JSONObject proxy = new JSONObject();
        proxy.put("tcpCount", tcpCount);
        proxy.put("httpCount", httpCount);
        proxy.put("httpsCount", httpsCount);
        proxy.put("total", tcpCount + httpCount + httpsCount);

        stats.put("clientTotal", ClientManager.getClientCount());
        stats.put("onlineClient", ChannelManager.onlineClientCount());
        stats.put("proxy", proxy);
        stats.put("runningTunnel", TcpProxyServer.get().runningPortCount());

        JSONObject sysConfig = ServiceFactory.INSTANCE.getSettingService().getAppConfig();
        res.put("stats", stats);
        res.put("sysConfig", sysConfig);
        return res;
    }

    public JSONObject getServerInfo() {
        return ServerHelper.getServerInfo();
    }
}
