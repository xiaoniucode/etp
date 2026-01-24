package com.xiaoniucode.etp.server.web.dao;

import com.xiaoniucode.etp.server.config.ConfigHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProxyDao extends BaseDao {
    public int insert(JSONObject data) {
        return jdbc.insert("INSERT INTO proxies (clientId, name, type,localIP, localPort, remotePort, status, source) VALUES (:clientId, :name, :type,:localIP, :localPort, :remotePort, :status, :source)")
                .bind("clientId", data.optInt("clientId"))
                .bind("name", data.optString("name"))
                .bind("type", data.optString("type"))
                .bind("localIP", data.optString("localIP"))
                .bind("localPort", data.optInt("localPort"))
                .bind("remotePort", data.optInt("remotePort"))
                .bind("status", data.optInt("status", 1))
                .bind("source", data.optInt("source"))
                .execute();
    }
    public JSONObject getProxyById(int id) {
        JSONObject proxy = jdbc.query("SELECT * FROM proxies WHERE id = :id")
                .bind("id", id)
                .one();
        if (proxy != null) {
            String type = proxy.optString("type");
            if ("http".equalsIgnoreCase(type) || "https".equalsIgnoreCase(type)) {
                JSONArray customDomains = DaoFactory.INSTANCE.getProxyDomainDao().listByProxyId(id);
                proxy.put("customDomains", customDomains);
            } else {
                proxy.put("customDomains", new JSONArray());
            }
        }
        return proxy;
    }

    public boolean updateProxy(JSONObject data) {
        long id = data.optLong("id");
        if (id <= 0) {
            return false;
        }

        StringBuilder set = new StringBuilder();
        List<Object> paramValues = new ArrayList<>();
        List<String> paramNames = new ArrayList<>();

        if (data.has("name")) {
            set.append("name = :name, ");
            paramNames.add("name");
            paramValues.add(data.getString("name"));
        }
        if (data.has("type")) {
            set.append("type = :type, ");
            paramNames.add("type");
            paramValues.add(data.getString("type"));
        }
        if (data.has("localIP")) {
            set.append("localIP = :localIP, ");
            paramNames.add("localIP");
            paramValues.add(data.getString("localIP"));
        }
        if (data.has("localPort")) {
            set.append("localPort = :localPort, ");
            paramNames.add("localPort");
            paramValues.add(data.getInt("localPort"));
        }
        if (data.has("remotePort")) {
            set.append("remotePort = :remotePort, ");
            paramNames.add("remotePort");
            paramValues.add(data.getInt("remotePort"));
        }
        if (data.has("status")) {
            set.append("status = :status, ");
            paramNames.add("status");
            paramValues.add(data.getInt("status"));
        }
        if (data.has("source")) {
            set.append("source = :source, ");
            paramNames.add("source");
            paramValues.add(data.getInt("source"));
        }

        set.setLength(Math.max(set.length() - 2, 0));
        if (!set.isEmpty()) {
            set.append(", updatedAt = datetime('now')");
        } else {
            set.append("updatedAt = datetime('now')");
        }

        String sql = "UPDATE proxies SET " + set + " WHERE id = :id";

        var updateBuilder = jdbc.update(sql);
        for (int i = 0; i < paramNames.size(); i++) {
            updateBuilder = updateBuilder.bind(paramNames.get(i), paramValues.get(i));
        }

        int rows = updateBuilder.bind("id", id).execute();

        if (rows > 0 && data.has("customDomains")) {
            JSONObject proxy = getProxyById((int) id);
            if (proxy != null && ("http".equalsIgnoreCase(proxy.optString("type")) || "https".equalsIgnoreCase(proxy.optString("type")))) {
                DaoFactory.INSTANCE.getProxyDomainDao().deleteByProxyId((int) id);

                JSONArray customDomains = data.getJSONArray("customDomains");
                for (int i = 0; i < customDomains.length(); i++) {
                    JSONObject domainObj = customDomains.getJSONObject(i);
                    String domain = domainObj.optString("domain");
                    if (!domain.isEmpty()) {
                        DaoFactory.INSTANCE.getProxyDomainDao().insert((int) id, domain);
                    }
                }
            }
        }

        return rows > 0;
    }

    public boolean deleteProxyById(int id) {
        DaoFactory.INSTANCE.getProxyDomainDao().deleteByProxyId(id);

        int rows = jdbc.update("DELETE FROM proxies WHERE id = :id")
                .bind("id", id)
                .execute();
        return rows > 0;
    }

    public JSONArray listAllProxies(String type) {
        String sql = """
                SELECT
                    p.id, p.clientId, p.name, p.type,p.localIP, p.localPort, p.remotePort, p.status,
                    p.createdAt, p.updatedAt, p.source, c.name AS clientName, c.secretKey
                FROM proxies p
                LEFT JOIN clients c ON p.clientId = c.id
                """;

        JSONArray proxies;
        if (type != null) {
            sql += " WHERE p.type = :type";
            proxies = jdbc.query(sql)
                    .bind("type", type)
                    .list();
        } else {
            proxies = jdbc.query(sql)
                    .list();
        }

        if (!proxies.isEmpty()) {
            JSONArray allDomains = DaoFactory.INSTANCE.getProxyDomainDao().listAllProxyDomains();

            HashMap<Integer, JSONArray> domainMap = new HashMap<>();
            for (int i = 0; i < allDomains.length(); i++) {
                JSONObject domainObj = allDomains.getJSONObject(i);
                int proxyId = domainObj.optInt("proxyId");
                String domain = domainObj.optString("domain");

                JSONArray customDomains = domainMap.computeIfAbsent(proxyId, k -> new JSONArray());

                JSONObject domainEntry = new JSONObject();
                domainEntry.put("domain", domain);
                customDomains.put(domainEntry);
            }

            for (int i = 0; i < proxies.length(); i++) {
                JSONObject proxy = proxies.getJSONObject(i);
                int proxyId = proxy.optInt("id");
                String proxyType = proxy.optString("type");

                if ("http".equalsIgnoreCase(proxyType)) {
                    JSONArray customDomains = domainMap.getOrDefault(proxyId, new JSONArray());
                    proxy.put("customDomains", customDomains);
                    proxy.put("httpProxyPort", ConfigHelper.get().getHttpProxyPort());
                } else if ("https".equalsIgnoreCase(proxyType)) {
                    JSONArray customDomains = domainMap.getOrDefault(proxyId, new JSONArray());
                    proxy.put("customDomains", customDomains);
                    proxy.put("httpsProxyPort", ConfigHelper.get().getHttpsProxyPort());
                } else {
                    proxy.put("customDomains", new JSONArray());
                }
            }
        }

        return proxies;
    }

    public int deleteProxiesByClient(int clientId) {
        // 先获取该客户端的所有代理ID
        JSONArray proxies = jdbc.query("SELECT id FROM proxies WHERE clientId = :clientId")
                .bind("clientId", clientId)
                .list();
        
        List<Integer> proxyIds = new ArrayList<>();
        for (int i = 0; i < proxies.length(); i++) {
            JSONObject proxy = proxies.getJSONObject(i);
            proxyIds.add(proxy.optInt("id"));
        }
        
        // 使用ProxyDomainDao删除相关域名
        if (!proxyIds.isEmpty()) {
            DaoFactory.INSTANCE.getProxyDomainDao().deleteByProxyIds(proxyIds);
        }

        return jdbc.update("DELETE FROM proxies WHERE clientId = :clientId")
                .bind("clientId", clientId)
                .execute();
    }

    public JSONObject getProxy(int clientId, String name) {
        JSONObject proxy = jdbc.query("SELECT * FROM proxies WHERE clientId = :clientId AND name = :name")
                .bind("clientId", clientId)
                .bind("name", name)
                .one();
        if (proxy != null) {
            String type = proxy.optString("type");
            int proxyId = proxy.optInt("id");
            if ("http".equalsIgnoreCase(type) || "https".equalsIgnoreCase(type)) {
                JSONArray customDomains = DaoFactory.INSTANCE.getProxyDomainDao().listByProxyId(proxyId);
                proxy.put("customDomains", customDomains);
            } else {
                proxy.put("customDomains", new JSONArray());
            }
        }
        return proxy;
    }

    public int deleteAllAutoRegisterProxy() {
        // 先获取所有自动注册的代理ID
        JSONArray proxies = jdbc.query("SELECT id FROM proxies WHERE source = 1").list();
        
        List<Integer> proxyIds = new ArrayList<>();
        for (int i = 0; i < proxies.length(); i++) {
            JSONObject proxy = proxies.getJSONObject(i);
            proxyIds.add(proxy.optInt("id"));
        }
        
        // 使用ProxyDomainDao删除相关域名
        if (!proxyIds.isEmpty()) {
            DaoFactory.INSTANCE.getProxyDomainDao().deleteByProxyIds(proxyIds);
        }

        return jdbc.update("DELETE FROM proxies WHERE source = 1")
                .execute();
    }
}