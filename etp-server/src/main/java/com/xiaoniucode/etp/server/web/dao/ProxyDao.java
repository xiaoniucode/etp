package com.xiaoniucode.etp.server.web.dao;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProxyDao extends BaseDao {
    public int insert(JSONObject data) {
        int proxyId = jdbc.insert("INSERT INTO proxies (clientId, name, type, localPort, remotePort, status, autoRegistered) VALUES (:clientId, :name, :type, :localPort, :remotePort, :status, :autoRegistered)")
                .bind("clientId", data.optInt("clientId"))
                .bind("name", data.optString("name"))
                .bind("type", data.optString("type"))
                .bind("localPort", data.optInt("localPort"))
                .bind("remotePort", data.optInt("remotePort"))
                .bind("status", data.optInt("status", 1))
                .bind("autoRegistered", data.optInt("autoRegistered"))
                .execute();
        
        if (proxyId > 0 && "http".equals(data.optString("type"))) {
            JSONArray domains = data.optJSONArray("domains");
            if (domains != null && !domains.isEmpty()) {
                for (int i = 0; i < domains.length(); i++) {
                    JSONObject domainObj = domains.getJSONObject(i);
                    String domain = domainObj.optString("domain");
                    if (!domain.isEmpty()) {
                        jdbc.insert("INSERT OR IGNORE INTO proxy_domains (proxyId, domain) VALUES (:proxyId, :domain)")
                                .bind("proxyId", proxyId)
                                .bind("domain", domain)
                                .execute();
                    }
                }
            }
        }
        
        return proxyId;
    }

    public JSONObject getById(int id) {
        JSONObject proxy = jdbc.query("SELECT * FROM proxies WHERE id = :id")
                .bind("id", id)
                .one();
        if (proxy != null) {
            String type = proxy.optString("type");
            if ("http".equals(type)) {
                JSONArray domains = jdbc.query("SELECT domain FROM proxy_domains WHERE proxyId = :proxyId")
                        .bind("proxyId", id)
                        .list();
                proxy.put("domains", domains);
            } else {
                proxy.put("domains", new JSONArray());
            }
        }
        return proxy;
    }

    public JSONArray listByClientId(long clientId) {
        JSONArray proxies = jdbc.query("SELECT * FROM proxies WHERE clientId = :clientId")
                .bind("clientId", clientId)
                .list();
        
        if (!proxies.isEmpty()) {
            JSONArray domains = jdbc.query("SELECT proxyId, domain FROM proxy_domains WHERE proxyId IN (SELECT id FROM proxies WHERE clientId = :clientId)")
                    .bind("clientId", clientId)
                    .list();
            
            java.util.HashMap<Integer, JSONArray> domainMap = new java.util.HashMap<>();
            for (int i = 0; i < domains.length(); i++) {
                JSONObject domainObj = domains.getJSONObject(i);
                int proxyId = domainObj.optInt("proxyId");
                String domain = domainObj.optString("domain");

                JSONArray proxyDomains = domainMap.computeIfAbsent(proxyId, k -> new JSONArray());

                JSONObject domainEntry = new JSONObject();
                domainEntry.put("domain", domain);
                proxyDomains.put(domainEntry);
            }
            
            for (int i = 0; i < proxies.length(); i++) {
                JSONObject proxy = proxies.getJSONObject(i);
                int proxyId = proxy.optInt("id");
                String type = proxy.optString("type");
                
                if ("http".equals(type)) {
                    proxy.put("domains", domainMap.getOrDefault(proxyId, new JSONArray()));
                } else {
                    proxy.put("domains", new JSONArray());
                }
            }
        }
        
        return proxies;
    }

    public JSONArray listActive() {
        JSONArray proxies = jdbc.query("SELECT * FROM proxies WHERE status = 1 ORDER BY clientId, remotePort")
                .list();
        
        if (!proxies.isEmpty()) {
            JSONArray domains = jdbc.query("SELECT proxyId, domain FROM proxy_domains WHERE proxyId IN (SELECT id FROM proxies WHERE status = 1)")
                    .list();
            
            java.util.HashMap<Integer, JSONArray> domainMap = new java.util.HashMap<>();
            for (int i = 0; i < domains.length(); i++) {
                JSONObject domainObj = domains.getJSONObject(i);
                int proxyId = domainObj.optInt("proxyId");
                String domain = domainObj.optString("domain");

                JSONArray proxyDomains = domainMap.computeIfAbsent(proxyId, k -> new JSONArray());

                JSONObject domainEntry = new JSONObject();
                domainEntry.put("domain", domain);
                proxyDomains.put(domainEntry);
            }
            
            for (int i = 0; i < proxies.length(); i++) {
                JSONObject proxy = proxies.getJSONObject(i);
                int proxyId = proxy.optInt("id");
                String type = proxy.optString("type");
                
                if ("http".equals(type)) {
                    proxy.put("domains", domainMap.getOrDefault(proxyId, new JSONArray()));
                } else {
                    proxy.put("domains", new JSONArray());
                }
            }
        }
        
        return proxies;
    }

    public boolean update(JSONObject data) {
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
        if (data.has("autoRegistered")) {
            set.append("autoRegistered = :autoRegistered, ");
            paramNames.add("autoRegistered");
            paramValues.add(data.getInt("autoRegistered"));
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
        
        if (rows > 0 && data.has("domains")) {
            JSONObject proxy = getById((int) id);
            if (proxy != null && "http".equals(proxy.optString("type"))) {
                jdbc.update("DELETE FROM proxy_domains WHERE proxyId = :proxyId")
                        .bind("proxyId", id)
                        .execute();
                
                JSONArray domains = data.getJSONArray("domains");
                for (int i = 0; i < domains.length(); i++) {
                    JSONObject domainObj = domains.getJSONObject(i);
                    String domain = domainObj.optString("domain");
                    if (!domain.isEmpty()) {
                        jdbc.insert("INSERT OR IGNORE INTO proxy_domains (proxyId, domain) VALUES (:proxyId, :domain)")
                                .bind("proxyId", id)
                                .bind("domain", domain)
                                .execute();
                    }
                }
            }
        }
        
        return rows > 0;
    }

    public boolean deleteById(int id) {
        jdbc.update("DELETE FROM proxy_domains WHERE proxyId = :proxyId")
                .bind("proxyId", id)
                .execute();
        
        int rows = jdbc.update("DELETE FROM proxies WHERE id = :id")
                .bind("id", id)
                .execute();
        return rows > 0;
    }

    public JSONArray listAllProxies(String type) {
        String sql = """
            SELECT
                p.id, p.clientId, p.name, p.type, p.localPort, p.remotePort, p.status,
                p.createdAt, p.updatedAt, p.autoRegistered, c.name AS clientName, c.secretKey
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
            JSONArray allDomains = jdbc.query("SELECT proxyId, domain FROM proxy_domains")
                    .list();
            
           HashMap<Integer, JSONArray> domainMap = new HashMap<>();
            for (int i = 0; i < allDomains.length(); i++) {
                JSONObject domainObj = allDomains.getJSONObject(i);
                int proxyId = domainObj.optInt("proxyId");
                String domain = domainObj.optString("domain");

                JSONArray domains = domainMap.computeIfAbsent(proxyId, k -> new JSONArray());

                JSONObject domainEntry = new JSONObject();
                domainEntry.put("domain", domain);
                domains.put(domainEntry);
            }
            
            for (int i = 0; i < proxies.length(); i++) {
                JSONObject proxy = proxies.getJSONObject(i);
                int proxyId = proxy.optInt("id");
                String proxyType = proxy.optString("type");
                
                if ("http".equals(proxyType)) {
                    JSONArray domains = domainMap.getOrDefault(proxyId, new JSONArray());
                    proxy.put("domains", domains);
                } else {
                    proxy.put("domains", new JSONArray());
                }
            }
        }
        
        return proxies;
    }

    public int deleteProxiesByClient(int clientId) {
        jdbc.update("DELETE FROM proxy_domains WHERE proxyId IN (SELECT id FROM proxies WHERE clientId = :clientId)")
                .bind("clientId", clientId)
                .execute();
        
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
            if ("http".equals(type)) {
                JSONArray domains = jdbc.query("SELECT domain FROM proxy_domains WHERE proxyId = :proxyId")
                        .bind("proxyId", proxyId)
                        .list();
                proxy.put("domains", domains);
            } else {
                proxy.put("domains", new JSONArray());
            }
        }
        return proxy;
    }

    public int deleteAllAutoRegisterProxy() {
        jdbc.update("DELETE FROM proxy_domains WHERE proxyId IN (SELECT id FROM proxies WHERE autoRegistered = 1)")
                .execute();
        
        return jdbc.update("DELETE FROM proxies WHERE autoRegistered = 1")
                .execute();
    }
}