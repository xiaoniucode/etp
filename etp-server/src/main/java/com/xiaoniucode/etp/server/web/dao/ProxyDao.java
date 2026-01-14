package com.xiaoniucode.etp.server.web.dao;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProxyDao extends BaseDao {
    public int insert(JSONObject data) {
        return jdbc.insert("INSERT INTO proxies (clientId, name, type, localPort, remotePort, domains, status, autoRegistered) VALUES (:clientId, :name, :type, :localPort, :remotePort, :domains, :status, :autoRegistered)")
                .bind("clientId", data.optInt("clientId"))
                .bind("name", data.optString("name"))
                .bind("type", data.optString("type"))
                .bind("localPort", data.optInt("localPort"))
                .bind("remotePort", data.optInt("remotePort"))
                .bind("domains", data.optString("domains", null))
                .bind("status", data.optInt("status", 1))
                .bind("autoRegistered", data.optInt("autoRegistered"))
                .execute();
    }

    public JSONObject getById(int id) {
        return jdbc.query("SELECT * FROM proxies WHERE id = :id")
                .bind("id", id)
                .one();
    }

    public JSONArray listByClientId(long clientId) {
        return jdbc.query("SELECT * FROM proxies WHERE clientId = :clientId")
                .bind("clientId", clientId)
                .list();
    }

    public JSONArray listActive() {
        return jdbc.query("SELECT * FROM proxies WHERE status = 1 ORDER BY clientId, remotePort")
                .list();
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
        if (data.has("domains")) {
            set.append("domains = :domains, ");
            paramNames.add("domains");
            paramValues.add(data.getInt("domains"));
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

        if (paramNames.isEmpty()) {
            return false;
        }

        set.setLength(set.length() - 2);
        set.append(", updatedAt = datetime('now')");

        String sql = "UPDATE proxies SET " + set + " WHERE id = :id";

        var updateBuilder = jdbc.update(sql);
        for (int i = 0; i < paramNames.size(); i++) {
            updateBuilder = updateBuilder.bind(paramNames.get(i), paramValues.get(i));
        }

        int rows = updateBuilder.bind("id", id).execute();
        return rows > 0;
    }

    public boolean deleteById(int id) {
        int rows = jdbc.update("DELETE FROM proxies WHERE id = :id")
                .bind("id", id)
                .execute();
        return rows > 0;
    }

    public JSONArray listAllProxies(String type) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("""
                SELECT
                    p.id,
                    p.clientId,
                    p.name,
                    p.type,
                    p.localPort,
                    p.remotePort,
                    p.domains,
                    p.status,
                    p.createdAt,
                    p.updatedAt,
                    p.autoRegistered,
                    c.name AS clientName,
                    c.secretKey
                FROM
                    proxies p
                LEFT JOIN clients c ON p.clientId = c.id
                """);

        if (type != null) {
            sqlBuilder.append(" WHERE p.type = :type");
            return jdbc.query(sqlBuilder.toString())
                    .bind("type", type)
                    .list();
        } else {
            return jdbc.query(sqlBuilder.toString())
                    .list();
        }
    }

    public int deleteProxiesByClient(int clientId) {
        return jdbc.update("DELETE FROM proxies WHERE clientId = :clientId")
                .bind("clientId", clientId)
                .execute();
    }

    public JSONObject getProxy(int clientId, String name) {
        return jdbc.query("SELECT * FROM proxies WHERE clientId = :clientId AND name = :name")
                .bind("clientId", clientId)
                .bind("name", name)
                .one();
    }

    public int deleteAllAutoRegisterProxy() {
        return jdbc.update("DELETE FROM proxies WHERE autoRegistered = 1")
                .execute();
    }
}